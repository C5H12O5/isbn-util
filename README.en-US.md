# ISBN-Util

[![Java CI](https://img.shields.io/github/actions/workflow/status/c5h12o5/isbn-util/code-coverage.yml?logo=github)](https://github.com/c5h12o5/isbn-util/actions/workflows/code-coverage.yml)
[![Codecov](https://img.shields.io/codecov/c/github/c5h12o5/isbn-util?logo=codecov)](https://app.codecov.io/gh/c5h12o5/isbn-util)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.c5h12o5/isbn-util?logo=apache-maven)](https://search.maven.org/artifact/io.github.c5h12o5/isbn-util)
[![Java Support](https://img.shields.io/badge/Java-8+-green?logo=oracle)](https://www.oracle.com/java/)
[![GitHub License](https://img.shields.io/github/license/c5h12o5/isbn-util?logo=apache&color=4D7A97)](LICENSE)

###### 📖 [简体中文](README.md) / 📖 English

This project is a simple ISBN utility written in Java. It can be used to parse, format and validate ISBNs.

* Supports both `13-digit` and `10-digit` ISBN formats.
* Supports importing the [RangeMessage.xml](https://www.isbn-international.org/range_file_generation) file from the [International ISBN Agency](https://www.isbn-international.org) to split and validate ISBNs.

## Getting started

#### 1. Installation

Maven:
```xml
<dependency>
  <groupId>io.github.c5h12o5</groupId>
  <artifactId>isbn-util</artifactId>
  <version>1.0.2</version>
</dependency>
```

Gradle:
```groovy
implementation 'io.github.c5h12o5:isbn-util:1.0.2'
```

#### 2. Usage

Remove hyphens or spaces from a separated ISBN code:
```java
ISBN.compact("7-03-014726-X")     = "703014726X"
ISBN.compact("978 7 03 038722 6") = "9787030387226"
```

Check if an ISBN code is a valid ISBN-13 or ISBN-10 format:
```java
ISBN.isValid("703014726X")        = true
ISBN.isValid("9787030387226")     = true
ISBN.isValid("978-7-03-038722-6") = true

// Check digit is incorrect
ISBN.isValid("978-7-03-038722-0") = false

// Separator position is incorrect
ISBN.isValid("978-7-0303-8722-6") = false
```

Format an ISBN code with specified separator:
```java
ISBN.formatISBN13("7-03-014726-X")      = "9787030147264"
ISBN.formatISBN13("703014726X", "-")    = "978-7-03-014726-4"
        
ISBN.formatISBN10("978-7-03-014726-4")  = "703014726X"
ISBN.formatISBN10("9787030147264", " ") = "7 03 014726 X"

// ISBN-13 with prefix 979 cannot be formatted as ISBN-10
ISBN.formatISBN10("979-8-6024-0545-3")  = null
```

Compare if two valid ISBN codes are equal:
```java
ISBN.equals("703014726X", "7 03 014726 X")     = true
ISBN.equals("703014726X", "978-7-03-014726-4") = true
```

Parse an ISBN code into an `ISBNObject`:
```java
ISBNObject obj = ISBN.parse("703014726X");

obj.getGS1Prefix();         // 978
obj.getRegistrationGroup(); // 7
obj.getRegistrant();        // 03
obj.getPublication();       // 014726
obj.getISBN13CheckDigit();  // 4
obj.getISBN10CheckDigit();  // X

obj.toISBN13();             // 9787030147264 
obj.toISBN10();             // 703014726X
```        

## Notes

The built-in RangeMessage.xml file maybe not up-to-date. If you need to use the latest range data, you can download the XML file from the International ISBN Agency website ([https://www.isbn-international.org/range_file_generation](https://www.isbn-international.org/range_file_generation)) and load it into memory by calling the `ISBN.loadRangeMessageFile` method:
```java
try (InputStream is = getClass().getResourceAsStream("/RangeMessage.xml")) {
    ISBN.loadRangeMessageFile(is);
}
```

## Documentation

* [JavaDoc](https://javadoc.io/doc/io.github.c5h12o5/isbn-util/latest/index.html)

## References

* [ISBN - Wikipedia](https://en.wikipedia.org/wiki/ISBN)
* [ISBN Users' Manual](https://www.isbn-international.org/sites/default/files/ISBN%20International%20Users%20Manual%20-%207th%20edition_absolutely_final.docx)

## License

[Apache-2.0 license](LICENSE)
