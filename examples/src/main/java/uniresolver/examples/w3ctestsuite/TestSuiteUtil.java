package uniresolver.examples.w3ctestsuite;

import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DID;
import uniresolver.result.DereferenceResult;
import uniresolver.result.ResolveResult;

import java.util.*;

public class TestSuiteUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static String makeIdentifierTestSuiteReport(String didMethod, List<String> dids, Map<String, String> didParameters) throws Exception {

        Map<String, Object> json = new LinkedHashMap<>();
        json.put("implementation", "Universal Resolver");
        json.put("implementer", "Decentralized Identity Foundation and Contributors");
        json.put("didMethod", didMethod);
        json.put("dids", dids);
        json.put("didParameters", didParameters);

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
    }

    static String makeDidResolutionTestSuiteReport(Map<String, List<Integer>> expectedOutcomes, List<String> function, List<String> didString, String didMethod, List<Map<String, Object>> resolutionOptions, List<ResolveResult> resolveResults) throws Exception {

        List<Object> executions = new ArrayList<>();

        for (int i=0; i<function.size(); i++) {

            Map<String, Object> input = new LinkedHashMap<>();
            input.put("did", didString.get(i));
            input.put("resolutionOptions", resolutionOptions.get(i));

            Map<String, Object> output = resolveResults.get(i).toMap();

            Map<String, Object> execution = new LinkedHashMap<>();
            execution.put("function", function.get(i));
            execution.put("input", input);
            execution.put("output", output);

            executions.add(execution);
        }

        Map<String, Object> json = new LinkedHashMap<>();
        json.put("implementation", "Universal Resolver");
        json.put("implementer", "Decentralized Identity Foundation and Contributors");
        json.put("didMethod", didMethod);
        json.put("expectedOutcomes", expectedOutcomes);
        json.put("executions", executions);

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
    }

    static String makeDidUrlDereferencingTestSuiteReport(Map<String, List<Integer>> expectedOutcomes, List<String> function, List<String> didUrlString, String didMethod, Map<String, Object> dereferenceOptions, List<DereferenceResult> dereferenceResults) throws Exception {

        List<Object> executions = new ArrayList<>();

        for (int i=0; i<function.size(); i++) {

            Map<String, Object> input = new LinkedHashMap<>();
            input.put("didUrl", didUrlString.get(i));
            input.put("dereferenceOptions", dereferenceOptions);

            Map<String, Object> output = dereferenceResults.get(i).toMap();

            Map<String, Object> execution = new LinkedHashMap<>();
            execution.put("function", function.get(i));
            execution.put("input", input);
            execution.put("output", output);

            executions.add(execution);
        }

        Map<String, Object> json = new LinkedHashMap<>();
        json.put("implementation", "Universal Resolver");
        json.put("implementer", "Decentralized Identity Foundation and Contributors");
        json.put("didMethod", didMethod);
        json.put("expectedOutcomes", expectedOutcomes);
        json.put("executions", executions);

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
    }
}
