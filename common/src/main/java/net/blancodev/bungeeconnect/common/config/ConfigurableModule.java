package net.blancodev.bungeeconnect.common.config;

import java.io.File;

/**
 * Interface representing a module that can be configured
 */
public interface ConfigurableModule {

    /**
     * @return the folder where the configuration files for this module should be stored
     */
    File getConfigurationFolder();

}
