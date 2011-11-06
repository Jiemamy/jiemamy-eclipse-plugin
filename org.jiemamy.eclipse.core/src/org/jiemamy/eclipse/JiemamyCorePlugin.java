/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2009/03/04
 *
 * This file is part of Jiemamy.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.jiemamy.eclipse;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import org.jiemamy.composer.ExportConfig;
import org.jiemamy.composer.Exporter;
import org.jiemamy.composer.ImportConfig;
import org.jiemamy.composer.Importer;
import org.jiemamy.dialect.Dialect;
import org.jiemamy.eclipse.extension.ExtensionResolver;
import org.jiemamy.eclipse.extension.validator.ModelValidator;

/**
 * Jiemamy Eclipse Core PluginのActivatorクラス。
 * 
 * @author daisuke
 */
public class JiemamyCorePlugin extends Plugin {
	
	/** The plug-in ID */
	public static final String PLUGIN_ID = "org.jiemamy.eclipse.core";
	
	/** プラグインクラスのシングルトンインスタンス */
	private static JiemamyCorePlugin plugin;
	
	/** 拡張ポイントに設定されたDialectのリゾルバ */
	private static ExtensionResolver<Dialect> dialectResolver;
	
	/** 拡張ポイントに設定されたImporterのリゾルバ */
	private static ExtensionResolver<Importer<ImportConfig>> importerResolver;
	
	/** 拡張ポイントに設定されたExporterのリゾルバ */
	private static ExtensionResolver<Exporter<ExportConfig>> exporterResolver;
	
	/** 拡張ポイントに設定されたModelValidatorのリゾルバ */
	private static ExtensionResolver<ModelValidator> validatorResolver;
	
	
	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static JiemamyCorePlugin getDefault() {
		return plugin;
	}
	
	/**
	 * 拡張ポイントに設定されたDialectのリゾルバを取得する。
	 * 
	 * @return 拡張ポイントに設定されたDialectのリゾルバ
	 */
	public static ExtensionResolver<Dialect> getDialectResolver() {
		assert dialectResolver != null;
		return dialectResolver;
	}
	
	/**
	 * 拡張ポイントに設定されたExporterのリゾルバを取得する。
	 * 
	 * @return 拡張ポイントに設定されたExporterのリゾルバ
	 */
	public static ExtensionResolver<Exporter<ExportConfig>> getExporterResolver() {
		assert exporterResolver != null;
		return exporterResolver;
	}
	
	/**
	 * 拡張ポイントに設定されたImporterのリゾルバを取得する。
	 * 
	 * @return 拡張ポイントに設定されたImporterのリゾルバ
	 */
	public static ExtensionResolver<Importer<ImportConfig>> getImporterResolver() {
		assert importerResolver != null;
		return importerResolver;
	}
	
	/**
	 * 拡張ポイントに設定されたサービスViewのリゾルバを取得する。
	 * 
	 * @return 拡張ポイントに設定されたサービスViewのリゾルバ
	 */
	public static ExtensionResolver<Exporter<ExportConfig>> getServiceViewResolver() {
		assert exporterResolver != null;
		return exporterResolver;
	}
	
	/**
	 * 拡張ポイントに設定されたModelValidatorのリゾルバを取得する。
	 * 
	 * @return 拡張ポイントに設定されたModelValidatorのリゾルバ
	 */
	public static ExtensionResolver<ModelValidator> getValidatorResolver() {
		assert validatorResolver != null;
		return validatorResolver;
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		plugin = this;
		dialectResolver = new ExtensionResolver<Dialect>(PLUGIN_ID, "dialects", "dialect", "class");
		importerResolver = new ExtensionResolver<Importer<ImportConfig>>(PLUGIN_ID, "importers", "importer", "class");
		exporterResolver = new ExtensionResolver<Exporter<ExportConfig>>(PLUGIN_ID, "exporters", "exporter", "class");
		validatorResolver = new ExtensionResolver<ModelValidator>(PLUGIN_ID, "validators", "dialect", "class");
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		dialectResolver = null;
		importerResolver = null;
		exporterResolver = null;
		validatorResolver = null;
		plugin = null;
		
		super.stop(context);
	}
	
}
