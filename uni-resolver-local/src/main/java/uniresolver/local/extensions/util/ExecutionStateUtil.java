package uniresolver.local.extensions.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.local.extensions.DereferencerExtension;
import uniresolver.local.extensions.ResolverExtension;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExecutionStateUtil {

    private static final String RESOLVER_EXTENSION_STAGES = "resolverExtensionStages";
    private static final String DEREFERENCER_EXTENSION_STAGES = "dereferencerExtensionStages";

    private static final Logger log = LoggerFactory.getLogger(ExecutionStateUtil.class);

    public static <E extends ResolverExtension> void addResolverExtensionStage(Map<String, Object> executionState, Class<E> extensionClass, ResolverExtension resolverExtension) {

        String extensionStage = extensionClass.getAnnotation(ResolverExtension.ExtensionStage.class).value();
        String extensionName = resolverExtension.getClass().getSimpleName();
        if (log.isDebugEnabled()) log.debug("Add resolver extension stage: " + extensionStage + " / " + extensionName);

        LinkedHashMap<String, List<String>> executionStateStages = (LinkedHashMap<String, List<String>>) executionState.computeIfAbsent(RESOLVER_EXTENSION_STAGES, f -> new LinkedHashMap<String, List<String>>());
        List<String> executionStateStagesExtensions = executionStateStages.computeIfAbsent(extensionStage, f -> new ArrayList<>());
        executionStateStagesExtensions.add(extensionName);
    }

    public static <E extends ResolverExtension> boolean checkResolverExtensionStage(Map<String, Object> executionState, Class<E> extensionClass, ResolverExtension resolverExtension) {

        String extensionStage = extensionClass.getAnnotation(ResolverExtension.ExtensionStage.class).value();
        String extensionName = resolverExtension.getClass().getSimpleName();
        if (log.isDebugEnabled()) log.debug("Check resolver extension stage: " + extensionStage + " / " + extensionName);

        LinkedHashMap<String, List<String>> executionStateStages = (LinkedHashMap<String, List<String>>) executionState.get(RESOLVER_EXTENSION_STAGES);
        if (executionStateStages == null) return false;

        List<String> executionStateStagesExtensions = executionStateStages.get(extensionStage);
        if (executionStateStagesExtensions == null) return false;

        return executionStateStagesExtensions.contains(extensionName);
    }

    public static <E extends DereferencerExtension> void addDereferencerExtensionStage(Map<String, Object> executionState, Class<E> extensionClass, DereferencerExtension dereferencerExtension) {

        String extensionStage = extensionClass.getAnnotation(DereferencerExtension.ExtensionStage.class).value();
        String extensionName = dereferencerExtension.getClass().getSimpleName();
        if (log.isDebugEnabled()) log.debug("Add dereferencer extension stage: " + extensionStage + " / " + extensionName);

        LinkedHashMap<String, List<String>> executionStateStages = (LinkedHashMap<String, List<String>>) executionState.computeIfAbsent(DEREFERENCER_EXTENSION_STAGES, f -> new LinkedHashMap<String, List<String>>());
        List<String> executionStateStagesExtensions = executionStateStages.computeIfAbsent(extensionStage, f -> new ArrayList<>());
        executionStateStagesExtensions.add(extensionName);
    }

    public static <E extends DereferencerExtension> boolean checkDereferencerExtensionStage(Map<String, Object> executionState, Class<E> extensionClass, DereferencerExtension dereferencerExtension) {

        String extensionStage = extensionClass.getAnnotation(DereferencerExtension.ExtensionStage.class).value();
        String extensionName = dereferencerExtension.getClass().getSimpleName();
        if (log.isDebugEnabled()) log.debug("Check dereferencer extension stage: " + extensionStage + " / " + extensionName);

        LinkedHashMap<String, List<String>> executionStateStages = (LinkedHashMap<String, List<String>>) executionState.get(DEREFERENCER_EXTENSION_STAGES);
        if (executionStateStages == null) return false;

        List<String> executionStateStagesExtensions = executionStateStages.get(extensionStage);
        if (executionStateStagesExtensions == null) return false;

        return executionStateStagesExtensions.contains(extensionName);
    }
}
