## metrics-store
metrics-store is a high performance api to store metrics/event data. It's implemented in Java. It uses timescaledb
which can store time series data points without losing performance

### Technologies/Tools Used
- [Spark Framework](http://sparkjava.com/)
- [Timescale](https://www.timescale.com/)
- [HikariCP](https://github.com/brettwooldridge/HikariCP)
- [Prometheus](https://prometheus.io/)
 
### Installation
Prerequisites
- Java 8
- Maven
- docker (optional)
- golang (optional)

### Getting Started
- Spin up timescale container
```sh
docker run --name metrics-store-timescale -e POSTGRES_DB=metrics-store -e POSTGRES_USER=metrics -e POSTGRES_PASSWORD=metrics@store -d -p 5432:5432 timescale/timescaledb
```
- Create extensions
```sh
docker exec -it metrics-store-timescale psql -U metrics -d metrics-store
CREATE EXTENSION IF NOT EXISTS "timescaledb";
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
```
- Spin up prometheus container
```sh
# Run this on root folder of repository
docker run --net=host --name metrics-store-prometheus -d -p 9090:9090 -v $(pwd)/prometheus.yml:/etc/prometheus/prometheus.yml prom/prometheus
```
- Navigate [http://localhost:9090](http://localhost:9090) in browser
- Too see the response time histogram, navigate to
[http://localhost:9090/graph?g0.range_input=10s&g0.expr=requests_latency_seconds_bucket&g0.tab=0](http://localhost:9090/graph?g0.range_input=10s&g0.expr=requests_latency_seconds_bucket&g0.tab=0)

- Install Metrics Store API
```sh
mvn clean install
```

- Run `Migrate.java` to create table migrations predefined in `config.apps`
```sh
mvn exec:java -Dexec.mainClass="Migrate"
``` 
- Run `Application.java` to spin up HTTP server
```sh
mvn exec:java -Dexec.mainClass="Application"
```
- Perform a sample request
```sh
# apiKey, timestamp & userId is required
# the rest can be anything
http post http://localhost:8080/events apiKey=df6653ce-3fb8-4dbc-8171-a0f98852357d userId=922337203685477 timestamp=1537876641 a=b c=d e=f

# The response should return 201 CREATED without response body
HTTP/1.1 201 Created
Content-Type: application/json
Date: Tue, 25 Sep 2018 21:03:36 GMT
Server: Jetty(9.4.8.v20171121)
Transfer-Encoding: chunked
```

### Benchmarking
- For benchmarks, install `hey`
```sh
go get -u github.com/rakyll/hey
```
- On repository root folder, run;
```sh
hey -n 100000 -c 100 -A "application/json" -D post.body.json -m POST http://localhost:8080/events
```
- You can change `sleepy` in `config.json` to `false` to bench effectively

### Future Improvements
- Tests
    - Unit tests
    - Integration tests
- Logging
    - Integrate [log4j](https://logging.apache.org/log4j/2.x/)
- API Key
    - Api key should be sent in request header
- Configuration
    - Make config unit testable by using `@JsonCreator` in constructor for each sub class
    - Integrate [etcd](https://coreos.com/etcd/) instead of hard coded config.json
        - By this, deployment would not be required for config changes
    - Multi environment configurations
    - Add max thread pool to config
    - Add `HikariConfig` options to config
- Database/Connection Pooling
    - Fine tune `HikariConfig`
- Command Line
    - Use a command line Framework
- Migrate
    - Make `chunk_time_interval` configurable
    - Convert Migrate to use `HikariDataSource` instead of JDBC single connection
    - Migrate is currently drop safe. Make it configurable using command line args