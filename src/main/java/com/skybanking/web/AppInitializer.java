package com.skybanking.web;

import com.skybanking.util.DBMigrations;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppInitializer implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			DBMigrations.ensureSchema();
		} catch (Exception ignored) { }
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// no-op
	}
}


