# Title

## Subtitle

An ordered list:

1. First item
2. Second item
3. Third Item

An unordered list:

* First item
* Second item
* Third item

*Italics* mixed with **bold**.

Unicode: ⌘⌥⇧⌃😂

![Image](Apple_park_cupertino_2019.jpg)

Inline `code`

Shell script:

```shell script
cd ~/Desktop
ls .
```

Scala:

```scala
package com.apple.geo.d3.datafinder.places.poi.query_templates

import com.apple.gemini.proto.Gemini.BusinessCardEvent
import com.apple.geo.d3.datafinder.driver.model.DataType
import com.apple.geo.d3.datafinder.driver.model.DataType.DataType
import com.apple.geo.d3.datafinder.places.utilities.takes.Takes100000
import com.google.protobuf.GeneratedMessage
import org.apache.spark.rdd.RDD
import play.api.libs.json.{JsNumber, JsValue}

abstract class BusinessCardEventMuidQuery extends Query with Takes100000 {
  def filter(businessCardEvent: BusinessCardEvent): Boolean
  final def apply(rddsByDataType: Map[DataType, RDD[_ <: GeneratedMessage]]): RDD[(String, JsValue)] = {
    val businessCardEvents = rddsByDataType(DataType.BusinessCardEvent)
    val results = businessCardEvents
      .map { case businessCardEvent: BusinessCardEvent => businessCardEvent }
      .filter(businessCardEvent => !businessCardEvent.getDeleted && filter(businessCardEvent))
      .map(businessCardEvent => JsNumber(businessCardEvent.getId))
      .take(amount)
      .map((name, _))
    businessCardEvents.context.parallelize(results)
  }
}
```

Swift:

```swift
let x = "test"
println(x)
```