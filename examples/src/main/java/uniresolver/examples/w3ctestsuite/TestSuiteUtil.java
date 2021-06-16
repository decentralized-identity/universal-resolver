package uniresolver.examples.w3ctestsuite;

import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DID;
import uniresolver.result.ResolveResult;

import java.util.Collections;
import java.util.Map;
import java.util.LinkedHashMap;

public class TestSuiteUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static String makeTestSuiteReport(String expectedOutcome, String function, String didString, String didMethod, Map<String, Object> resolutionOptions, ResolveResult resolveResult) throws Exception {

        Map<String, Object> expectedOutcomes = new LinkedHashMap<>();
        expectedOutcomes.put(expectedOutcome, Collections.singletonList(0));

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("did", didString);
        input.put("resolutionOptions", resolutionOptions);

        Map<String, Object> output = resolveResult.toMap();

        Map<String, Object> execution = new LinkedHashMap<>();
        execution.put("function", function);
        execution.put("input", input);
        execution.put("output", output);

        Map<String, Object> json = new LinkedHashMap<>();
        json.put("implementation", "Universal Resolver");
        json.put("implementer", "Decentralized Identity Foundation and Contributors");
        json.put("didMethod", didMethod);
        json.put("expectedOutcomes", expectedOutcomes);
        json.put("executions", Collections.singletonList(execution));

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
    }
}
