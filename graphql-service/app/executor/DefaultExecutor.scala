package executor

import javax.inject._
import scala.concurrent.ExecutionContext

@Singleton
class DefaultExecutor @Inject()(implicit val ec: ExecutionContext)




