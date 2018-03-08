package mesosphere.marathon
package api.v2.json

import mesosphere.UnitTest
import mesosphere.marathon.api.JsonTestHelper
import mesosphere.marathon.core.appinfo.{ AppInfo, TaskCounts }
import mesosphere.marathon.core.readiness.{ HttpResponse, ReadinessCheckResult }
import mesosphere.marathon.core.task.Task
import mesosphere.marathon.state._
import org.apache.mesos.{ Protos => mesos }
import play.api.libs.json.{ JsObject, Json }

import scala.collection.immutable.Seq

class AppDefinitionAppInfoTest extends UnitTest {
  import Formats._

  val app = AppDefinition(PathId("/test"), cmd = Some("sleep 123"))

  val counts = TaskCounts(
    tasksStaged = 3,
    tasksRunning = 5,
    tasksHealthy = 4,
    tasksUnhealthy = 1
  )

  val readinessCheckResults = Seq(
    ReadinessCheckResult("foo", Task.Id.forRunSpec(app.id), false, Some(HttpResponse(503, "text/plain", "n/a")))
  )

  val deployments = Seq(
    Identifiable("deployment1")
  )

  "AppDefinitionAppInfo" should {
    "app with taskCounts" in {
      Given("an app with counts")
      val extended = AppInfo(app, maybeCounts = Some(counts))

      Then("the result contains all fields of the app plus the counts")
      val expectedJson = Json.toJson(app).as[JsObject] ++ Json.obj(
        "tasksStaged" -> 3,
        "tasksRunning" -> 5,
        "tasksHealthy" -> 4,
        "tasksUnhealthy" -> 1
      )
      JsonTestHelper.assertThatJsonOf(extended).correspondsToJsonOf(expectedJson)
    }

    "app with deployments" in {
      Given("an app with deployments")
      val extended = AppInfo(app, maybeDeployments = Some(deployments))

      Then("the result contains all fields of the app plus the deployments")
      val expectedJson = Json.toJson(app).as[JsObject] ++ Json.obj(
        "deployments" -> Seq(Json.obj("id" -> "deployment1"))
      )
      JsonTestHelper.assertThatJsonOf(extended).correspondsToJsonOf(expectedJson)
    }

    "app with readiness results" in {
      Given("an app with deployments")
      val extended = AppInfo(app, maybeReadinessCheckResults = Some(readinessCheckResults))

      Then("the result contains all fields of the app plus the deployments")
      val expectedJson = Json.toJson(app).as[JsObject] ++ Json.obj(
        "readinessCheckResults" -> Seq(Json.obj(
          "name" -> "foo",
          "taskId" -> "foo",
          "ready" -> false,
          "lastResponse" -> Json.obj(
            "status" -> 503,
            "contentType" -> "text/plain",
            "body" -> "n/a"
          )
        ))
      )
      JsonTestHelper.assertThatJsonOf(extended).correspondsToJsonOf(expectedJson)
    }

    "app with taskCounts + deployments (show that combinations work)" in {
      Given("an app with counts")
      val extended = AppInfo(app, maybeCounts = Some(counts), maybeDeployments = Some(deployments))

      Then("the result contains all fields of the app plus the counts")
      val expectedJson =
        Json.toJson(app).as[JsObject] ++
          Json.obj(
            "tasksStaged" -> 3,
            "tasksRunning" -> 5,
            "tasksHealthy" -> 4,
            "tasksUnhealthy" -> 1
          ) ++ Json.obj(
              "deployments" -> Seq(Json.obj("id" -> "deployment1"))
            )
      JsonTestHelper.assertThatJsonOf(extended).correspondsToJsonOf(expectedJson)
    }

    "app with lastTaskFailure" in {
      Given("an app with a lastTaskFailure")
      val lastTaskFailure = new TaskFailure(
        appId = PathId("/myapp"),
        taskId = mesos.TaskID.newBuilder().setValue("myapp.2da6109e-4cce-11e5-98c1-be5b2935a987").build(),
        state = mesos.TaskState.TASK_FAILED,
        message = "Command exited with status 1",
        host = "srv2.dc43.mesosphere.com",
        timestamp = Timestamp("2015-08-27T15:13:48.386Z"),
        version = Timestamp("2015-08-27T14:13:05.942Z"),
        slaveId = Some(mesos.SlaveID.newBuilder().setValue("slave34").build())
      )
      val extended = AppInfo(app, maybeLastTaskFailure = Some(lastTaskFailure))

      Then("the result contains all fields of the app plus the deployments")
      val lastTaskFailureJson = Json.parse(
        """
       | {
       |   "lastTaskFailure": {
       |     "appId": "/myapp",
       |     "host": "srv2.dc43.mesosphere.com",
       |     "message": "Command exited with status 1",
       |     "state": "TASK_FAILED",
       |     "taskId": "myapp.2da6109e-4cce-11e5-98c1-be5b2935a987",
       |     "slaveId": "slave34",
       |     "timestamp": "2015-08-27T15:13:48.386Z",
       |     "version": "2015-08-27T14:13:05.942Z"
       |   }
       | }
       |""".stripMargin('|')).as[JsObject]
      val expectedJson = Json.toJson(app).as[JsObject] ++ lastTaskFailureJson
      JsonTestHelper.assertThatJsonOf(extended).correspondsToJsonOf(expectedJson)
    }
  }
}