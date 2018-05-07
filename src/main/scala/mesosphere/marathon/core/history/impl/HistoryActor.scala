package mesosphere.marathon
package core.history.impl

import akka.actor.Actor
import akka.event.EventStream
import com.typesafe.scalalogging.StrictLogging
import mesosphere.marathon.core.event._
import mesosphere.marathon.state.TaskFailure
import mesosphere.marathon.storage.repository.TaskFailureRepository

// TODO(PODS): Move from Task to Instance
class HistoryActor(eventBus: EventStream, taskFailureRepository: TaskFailureRepository)
  extends Actor with StrictLogging {

  override def preStart(): Unit = {
    // TODO(cleanup): adjust InstanceChanged to be able to replace using MesosStatusUpdateEvent here (#4792)
    eventBus.subscribe(self, classOf[MesosStatusUpdateEvent])
    eventBus.subscribe(self, classOf[UnhealthyInstanceKillEvent])
    eventBus.subscribe(self, classOf[AppTerminatedEvent])

    logger.info("History actor ready")
  }

  def receive: Receive = {

    case m @ TaskFailure.FromUnhealthyInstanceKillEvent(taskFailure) =>
      taskFailureRepository.store(taskFailure)

    case m @ TaskFailure.FromMesosStatusUpdateEvent(taskFailure) =>
      println("mesos update")
      println(m)
      taskFailureRepository.store(taskFailure)

    case _: MesosStatusUpdateEvent => // ignore non-failure status updates

    case AppTerminatedEvent(appId, eventType, timestamp) =>
      taskFailureRepository.delete(appId)
  }
}
