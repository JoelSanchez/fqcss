## 0.1.0 (2017-05-30)

Bugfixes:

  - Generate the namespace identifiers using clojure.core/hash instead of relying on the pseudo-gensym atom. This approach generated different identifiers between the server and the client, depending on the order of generation.