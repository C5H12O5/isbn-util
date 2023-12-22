# ISBN-Util

[![Java CI](https://img.shields.io/github/actions/workflow/status/c5h12o5/isbn-util/code-coverage.yml?logo=github)](https://github.com/c5h12o5/isbn-util/actions/workflows/code-coverage.yml)
[![Codecov](https://img.shields.io/codecov/c/github/c5h12o5/isbn-util?logo=codecov)](https://app.codecov.io/gh/c5h12o5/isbn-util)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.c5h12o5/isbn-util?logo=apache-maven)](https://search.maven.org/artifact/io.github.c5h12o5/isbn-util)
[![Java Support](https://img.shields.io/badge/Java-8+-green?logo=oracle)](https://www.oracle.com/java/)
[![GitHub License](https://img.shields.io/github/license/c5h12o5/isbn-util?logo=apache&color=4D7A97)](LICENSE)

###### 📖 简体中文 / 📖 [English](README.en-US.md)

本项目是一个简单的 Java ISBN 工具类，可以用于解析、格式化和验证 ISBN 编号。

* 支持`13位国际标准书号`和`10位国际标准书号`两种格式。
* 支持导入来自 [国际ISBN中心](https://www.isbn-international.org) 的 [区域代码表](https://www.isbn-international.org/range_file_generation) 文件来对书号进行分割和验证。

## 快速开始

#### 1. 添加依赖

Maven：
```xml
<dependency>
  <groupId>io.github.c5h12o5</groupId>
  <artifactId>isbn-util</artifactId>
  <version>1.0.2</version>
</dependency>
```

Gradle：
```groovy
implementation 'io.github.c5h12o5:isbn-util:1.0.2'
```

#### 2. 使用示例

去除 ISBN 编号中的分隔符：
```java
ISBN.compact("7-03-014726-X")     = "703014726X"
ISBN.compact("978 7 03 038722 6") = "9787030387226"
```

检查 ISBN 编号是否为有效的 ISBN-13 或 ISBN-10 格式：
```java
ISBN.isValid("703014726X")        = true
ISBN.isValid("9787030387226")     = true
ISBN.isValid("978-7-03-038722-6") = true

// 校验位错误
ISBN.isValid("978-7-03-038722-0") = false

// 分隔符位置错误
ISBN.isValid("978-7-0303-8722-6") = false
```

使用指定的分隔符格式化 ISBN 编号：
```java
ISBN.formatISBN13("7-03-014726-X")      = "9787030147264"
ISBN.formatISBN13("703014726X", "-")    = "978-7-03-014726-4"

ISBN.formatISBN10("978-7-03-014726-4")  = "703014726X"
ISBN.formatISBN10("9787030147264", " ") = "7 03 014726 X"

// 带有 979 前缀的 ISBN-13 无法格式化为 ISBN-10
ISBN.formatISBN10("979-8-6024-0545-3")  = null
```

比较两个有效的 ISBN 编号是否相等：
```java
ISBN.equals("703014726X", "7 03 014726 X")     = true
ISBN.equals("703014726X", "978-7-03-014726-4") = true
```

将 ISBN 编号解析为 ISBN 对象：
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

## 需要注意

本项目内置的区域代码表文件可能不是最新的，如果你需要使用最新的区域代码数据，可以从国际ISBN中心网站（[https://www.isbn-international.org/range_file_generation](https://www.isbn-international.org/range_file_generation)）上下载最新文件到本地，然后调用`ISBN.loadRangeMessageFile`方法将其加载到内存中：
```java
try (InputStream is = getClass().getResourceAsStream("/RangeMessage.xml")) {
    ISBN.loadRangeMessageFile(is);
}
```

## API文档

* [JavaDoc](https://javadoc.io/doc/io.github.c5h12o5/isbn-util/latest/index.html)

## 参考文献

* [ISBN - 维基百科](https://zh.wikipedia.org/zh-cn/%E5%9B%BD%E9%99%85%E6%A0%87%E5%87%86%E4%B9%A6%E5%8F%B7)
* [ISBN 用户手册](https://www.isbn-international.org/sites/default/files/ISBN%20users%27%20Manual%202017-simplified%20chinese%20%28Chinese%20translation%20of%20seventh%20edition%29.pdf)

## 使用许可

[Apache-2.0 license](LICENSE)
