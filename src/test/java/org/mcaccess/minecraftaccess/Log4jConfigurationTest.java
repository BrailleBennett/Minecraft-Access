package org.mcaccess.minecraftaccess;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Log4jConfigurationTest {
    @Test
    void testLog4jYamlConfiguration() throws Exception {
        File configFile = new File("log4j.yaml");
        assertThat(configFile).exists();

        try (InputStream in = new FileInputStream(configFile)) {
            ConfigurationSource source = new ConfigurationSource(in, configFile);
            ConfigurationFactory factory = ConfigurationFactory.getInstance();
            LoggerContext context = LoggerContext.getContext(false);
            Configuration config = factory.getConfiguration(context, source);
            config.initialize();
            config.start();

            assertThat(config).isNotNull();
            assertThat(config.getRootLogger().getLevel()).isEqualTo(Level.INFO);
            assertThat(config.getRootLogger().getAppenderRefs()).hasSize(4);

            LoggerConfig root = config.getRootLogger();
            assertThat(root.getAppenderRefs())
                    .extracting("ref")
                    .containsExactlyInAnyOrder("SysOut", "DebugFile", "LatestFile", "ServerGuiConsole");

            LoggerConfig loggerConfig = config.getLoggerConfig("org.mcaccess.minecraftaccess");
            assertThat(loggerConfig.getLevel()).isEqualTo(Level.DEBUG);

            LoggerConfig cameraConfig = config.getLoggerConfig("org.mcaccess.minecraftaccess.features.CameraControls");
            assertThat(cameraConfig.getLevel()).isEqualTo(Level.TRACE);

            LoggerConfig mixinConfig = config.getLoggerConfig("org.mcaccess.minecraftaccess.mixin");
            assertThat(mixinConfig.getLevel()).isEqualTo(Level.DEBUG);
        }
    }

    @Test
    void testCompositeConfiguration() throws Exception {
        File loomConfig = new File(".gradle/loom-cache/projects/fabric/log4j.xml");
        File projectConfig = new File("log4j.yaml");
        if (!loomConfig.exists()) {
            return;
        }

        System.setProperty("log4j.configurationFile", loomConfig.getAbsolutePath() + "," + projectConfig.getAbsolutePath());
        LoggerContext context = (LoggerContext) org.apache.logging.log4j.LogManager.getContext(false);
        context.reconfigure();
        Configuration config = context.getConfiguration();

        LoggerConfig cameraConfig = config.getLoggerConfig("org.mcaccess.minecraftaccess.features.CameraControls");
        assertThat(cameraConfig.getLevel()).isEqualTo(Level.TRACE);

        org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger("org.mcaccess.minecraftaccess.features.CameraControls");
        assertThat(logger.isTraceEnabled()).isTrue();
        assertThat(logger.isDebugEnabled()).isTrue();
    }
}
