package modules

import scala.concurrent.Future

import javax.inject._

import com.google.inject.{ AbstractModule, Provider }
import com.google.inject.name.Names
import com.zaxxer.hikari.HikariDataSource

import play.api.inject.ApplicationLifecycle

import ixias.slick.jdbc.MySQLProfile.api._
import ixias.slick.builder.{ DatabaseBuilder, HikariConfigBuilder }
import ixias.slick.model.DataSourceName

// DatabaseModule: @Named("master") / @Named("slave") の Database を DI コンテナに登録する
//~~~~~~~~~~~~~~~~~~~~~~
class DatabaseModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[Database])
      .annotatedWith(Names.named("master"))
      .toProvider(classOf[MasterDatabaseProvider])
      .asEagerSingleton()

    bind(classOf[Database])
      .annotatedWith(Names.named("slave"))
      .toProvider(classOf[SlaveDatabaseProvider])
      .asEagerSingleton()
  }
}

// DataSourceName から HikariCP の接続プールを構築し、Slick の Database を生成する Provider の共通実装
//~~~~~~~~~~~~~~~~~~~~~~
abstract class DatabaseProvider(lifecycle: ApplicationLifecycle) extends Provider[Database] {

  protected def dsn: DataSourceName

  private lazy val dataSource: HikariDataSource = {
    val config = HikariConfigBuilder.default(dsn).build()
    config.validate()
    new HikariDataSource(config)
  }

  lifecycle.addStopHook { () =>
    Future.successful(dataSource.close())
  }

  override def get(): Database =
    DatabaseBuilder.fromHikariDataSource(dataSource)
}

@Singleton
class MasterDatabaseProvider @Inject() (lifecycle: ApplicationLifecycle)
    extends DatabaseProvider(lifecycle) {
  override protected val dsn: DataSourceName = DataSourceName("ixias.db.mysql://master/to_do")
}

@Singleton
class SlaveDatabaseProvider @Inject() (lifecycle: ApplicationLifecycle)
    extends DatabaseProvider(lifecycle) {
  override protected val dsn: DataSourceName = DataSourceName("ixias.db.mysql://slave/to_do")
}
