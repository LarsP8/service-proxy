/* Copyright 2009, 2021 predic8 GmbH, www.predic8.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */
package com.predic8.membrane.annot.generator.kubernetes;

import com.predic8.membrane.annot.model.MainInfo;
import com.predic8.membrane.annot.model.Model;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Autogenerates a helper file for JSON parsing
 */
public class K8sHelperGenerator extends AbstractK8sGenerator {

    public K8sHelperGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    protected String fileName() {
        return K8sHelperGenerator.class.getSimpleName() + "AutoGenerated";
    }

    @Override
    protected void write(Model m) {
        m.getMains().forEach(main -> {
            try {
                List<Element> sources = new ArrayList<>(main.getInterceptorElements());
                sources.add(main.getElement());

                FileObject fileObject = processingEnv.getFiler().createSourceFile(
                        main.getAnnotation().outputPackage() + "." + fileName(),
                        sources.toArray(new Element[0]));

                try (BufferedWriter w = new BufferedWriter(fileObject.openWriter())) {
                    assemble(w, main);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void assemble(Writer w, MainInfo main) throws IOException {
        writeCopyright(w);
        writeClassContent(w, main);
    }

    private void writeCopyright(Writer w) throws IOException {
        appendLine(w,
                "/* Copyright 2021 predic8 GmbH, www.predic8.com",
                "",
                "   Licensed under the Apache License, Version 2.0 (the \"License\");",
                "   you may not use this file except in compliance with the License.",
                "   You may obtain a copy of the License at",
                "",
                "   http://www.apache.org/license/LICENSE-2.0",
                "",
                "   Unless required by applicable law or agreed to in writing, software",
                "   distributed under the License is distributed on an \"AS IS\" BASIS,",
                "   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.",
                "   See the License for the specific language governing permissions and",
                "   limitations under the License.",
                "*/"
        );
    }

    private void writeClassContent(Writer w, MainInfo mainInfo) throws IOException {
        appendLine(w,
                "",
                "package " + mainInfo.getAnnotation().outputPackage() + ";",
                "",
                "import com.predic8.membrane.core.rules.Rule;",
                "import com.predic8.membrane.core.interceptor.Interceptor;",
                "",
                "import java.util.Map;",
                "import java.util.List;",
                "import java.util.HashMap;",
                "import java.util.ArrayList;",
                "",
                "/**",
                "  * Automatically generated by {@link " + K8sHelperGenerator.class.getName() + "}",
                "  */",
                "public class " + fileName() + " {",
                "    public static Map<String, Class<?>> elementMapping = new HashMap<>();",
                "    public static List<String> crdPluralNames = new ArrayList<>();",
                "",
                "    static {",
                        "",
                        assembleCrdPluralNames(mainInfo),
                        "",
                        assembleElementMapping(mainInfo),
                "    }",
                "}"
        );
    }

    private String assembleElementMapping(MainInfo main) {
        return main.getIis().stream()
                .map(ei -> String.format("        elementMapping.put(\"%s\", %s.class);" + System.lineSeparator()
                                + "        elementMapping.put(\"%s\", %s.class);",
                        ei.getAnnotation().name(),
                        ei.getElement().getQualifiedName(),
                        ei.getAnnotation().name().toLowerCase(),
                        ei.getElement().getQualifiedName()))
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private String assembleCrdPluralNames(MainInfo main) {
        return getRulesStream(main)
                .map(ei -> pluralize(ei.getAnnotation().name().toLowerCase()))
                .map(ei -> "        crdPluralNames.add(\"" + ei + "\");")
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
