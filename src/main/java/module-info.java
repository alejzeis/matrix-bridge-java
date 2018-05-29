module io.github.jython234.matrix.bridge {
    requires java.base;

    requires static lombok;

    requires slf4j.api;
    requires io.github.jython234.matrix.appservice;

    exports io.github.jython234.matrix.bridge;
}