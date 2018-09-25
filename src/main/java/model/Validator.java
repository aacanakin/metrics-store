package model;

import config.Config;

import java.util.Map;

interface Validator {
    void validate(final Config config, final Map<String, Object> body) throws Exception;
}
