package modules

import scaldi.Module
import request.{DispatchTaskServerRequests, TaskServerRequests}

class HttpModule extends Module {
  bind[TaskServerRequests] to new DispatchTaskServerRequests
}
