About this example
==================
    This example illustrates how to hook the EdiSax (http://milyn.codehaus.org/EdiSax)
    EDI parser into a Smooks based transform (filter operation).  It's another example
    of how non-XML streams can be processed by Smooks (see the "csv-to-xml" example).

    In this example, we simply configure in the EdiSax parser to process the EDI
    stream into XML.  We don't perform any other transforms on the underlying data. For
    an example of how other transform operations can be built on top of this edi-to-xml
    transform, see the "edi-to-java" example.

    See:
        1. The "Main" class in src/main/java/example/Main.java.
        2. The input message in input-message.edi.
        3. smooks-config.xml.
        4. The EdiSax parser configuration in
           src/main/java/example/edi-to-xml-order-mapping.xml
        5. http://milyn.codehaus.org/EdiSax

How to Run?
===========
    Requirements:
        1. JDK 1.8 (sdk install java 8.0.372-amzn)
        2. Maven 3.x (http://maven.apache.org/download.html)

    Running:
        1. ** Before running, change the string `x12Format`(located in Main.java) to the desired format: "271", "834", "837", or "order"**
        2. "mvn clean install"
        3. "mvn exec:java" (currently just running java from within VSCode)
            (I am currently just running Main.java from within VSCode)

Troubleshooting
===========
    Exception in thread "main" org.smooks.api.SmooksException: Failed to filter source
    ...
    Possible Cause: Too many trailing segment terminators in input-message.edi