package com.blamejared.crafttweaker_annotation_processors.processors.document;

import com.blamejared.crafttweaker_annotations.annotations.Document;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

@SupportedAnnotationTypes({"com.blamejared.crafttweaker_annotations.annotations.Document", "net.minecraftforge.fml.common.Mod"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class DocumentProcessorNew extends AbstractProcessor {
    private static final File docsOut = new File("docsOut");
    private static final Set<CrafttweakerDocumentationPage> pages = new TreeSet<>(Comparator.comparing(CrafttweakerDocumentationPage::getDocumentTitle));
    public static Map<String, String> modIdByPackage = new HashMap<>();

    public static String getModIdForPackage(Element element, ProcessingEnvironment environment) {
        final String packageName = environment.getElementUtils().getPackageOf(element).getQualifiedName().toString();
        for (String knownPackName : modIdByPackage.keySet()) {
            if (packageName.startsWith(knownPackName)) {
                return modIdByPackage.get(knownPackName);
            }
        }
        return null;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        fillModIdInfo(roundEnv);

        for (Element element : roundEnv.getElementsAnnotatedWith(Document.class)) {
            final Document document = element.getAnnotation(Document.class);
            if (document == null) {
                this.processingEnv.getMessager()
                        .printMessage(Diagnostic.Kind.ERROR, "Internal error! Document annotation null", element);
                continue;
            }

            if (!element.getKind().isClass() && !element.getKind().isInterface()) {
                this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "How is this annotated", element);
                continue;
            }


            final TypeElement typeElement = (TypeElement) element;
            final CrafttweakerDocumentationPage documentationPage = CrafttweakerDocumentationPage.convertType(typeElement, this.processingEnv);
            if (documentationPage != null) {
                pages.add(documentationPage);
            }
        }

        if (roundEnv.processingOver()) {
            clearOutputDir();
            writeToFiles();
        }
        return false;
    }

    private void clearOutputDir() {
        if (docsOut.exists()) {
            if (!docsOut.isDirectory()) {
                throw new IllegalStateException("File " + docsOut + " exists and is not a directory!");
            }

            try {
                Files.walkFileTree(docsOut.getAbsoluteFile().toPath(), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return super.visitFile(file, attrs);
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return super.postVisitDirectory(dir, exc);
                    }
                });
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.toString());
            }
        }
    }

    private void fillModIdInfo(RoundEnvironment roundEnv) {
        final TypeElement typeElement = processingEnv.getElementUtils()
                .getTypeElement("net.minecraftforge.fml.common.Mod");
        outer:
        for (Element element : roundEnv.getElementsAnnotatedWith(typeElement)) {
            for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
                if (annotationMirror.getAnnotationType().asElement().equals(typeElement)) {
                    final Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = annotationMirror
                            .getElementValues();

                    for (ExecutableElement executableElement : elementValues.keySet()) {
                        if (executableElement.getSimpleName().toString().equals("value")) {
                            final String packageName = processingEnv.getElementUtils()
                                    .getPackageOf(element)
                                    .getQualifiedName()
                                    .toString();
                            modIdByPackage.put(packageName, elementValues.get(executableElement)
                                    .getValue()
                                    .toString());
                            continue outer;
                        }
                    }
                }
            }
            processingEnv.getMessager()
                    .printMessage(Diagnostic.Kind.ERROR, "Internal error: Could not find mod-id for this element!", element);
        }
    }

    private void writeToFiles() {
        //Create folder
        if (!docsOut.exists() && !docsOut.mkdirs()) {
            processingEnv.getMessager()
                    .printMessage(Diagnostic.Kind.ERROR, "Could not create folder " + docsOut.getAbsolutePath());
            return;
        }

        //Create files
        try {
            for (CrafttweakerDocumentationPage page : pages) {
                page.write(docsOut, processingEnv);
            }
            writeYAML();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeYAML() throws IOException {
        final File mkdocsFile = new File(docsOut, "mkdocs.yml");
        try (final PrintWriter writer = new PrintWriter(new FileWriter(mkdocsFile))) {
            final List<CrafttweakerDocumentationPage> values = new ArrayList<>(CrafttweakerDocumentationPage.knownTypes.values());
            values.sort(Comparator.comparing(CrafttweakerDocumentationPage::getDocPath));
            for (CrafttweakerDocumentationPage value : values) {
                writer.printf("  - %s: '%s.md'%n", value.getDocumentTitle(), value.getDocPath());
            }
        }
    }
}