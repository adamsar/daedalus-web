package modules

import controllers.EntityController
import scaldi.Module

class WebModule extends Module{
  binding to new EntityController()
}
