(ns immutant.init
  (:require [immutant.web :as web]
            [io.pedestal.service.http :as http]
            [authority.service :as app]))

(web/start-servlet "/" (::http/servlet (http/create-servlet app/service)))
