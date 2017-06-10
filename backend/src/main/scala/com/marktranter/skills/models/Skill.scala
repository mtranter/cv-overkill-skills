package com.marktranter.skills.models

import reactivemongo.bson.Macros.Annotations.Key

/**
  * Created by mark on 29/05/17.
  */
case class Skill(@Key("_id") name: String, skillLevel: Int, tags: Set[String])