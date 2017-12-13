package com.suprnation.cms.deserialiser

trait CmsDeserialiser[T] {

    def deserialise(content:String):T

}
