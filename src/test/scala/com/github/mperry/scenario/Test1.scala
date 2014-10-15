package com.github.mperry.scenario

import io.gatling.core.Predef._
//import bootstrap._
import io.gatling.http.Predef._

class Test1 extends Simulation {

  val httpConf = http
    .baseURL("http://localhost:8094/server")
    .acceptHeader("application/json")

  val testScn = scenario("test")
    .repeat(10) {
    exec(http("hello world").get("/"))
  }

  setUp(testScn.inject(atOnceUsers(2)).protocols(httpConf))
}