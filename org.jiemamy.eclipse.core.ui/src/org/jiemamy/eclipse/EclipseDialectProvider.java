/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
 * Created on 2008/11/06
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

import org.eclipse.core.runtime.CoreException;

import org.jiemamy.dialect.Dialect;
import org.jiemamy.eclipse.utils.ExceptionHandler;

/**
 * Eclipse環境においての{@link Dialect}インスタンス取得戦略クラス。
 * 
 * @author daisuke
 */
public class EclipseDialectProvider implements InstanceProvider<Dialect> {
	
	public Dialect getInstance(String fqcn) {
		try {
			return JiemamyCorePlugin.getDialectResolver().getInstance(fqcn);
		} catch (CoreException e) {
			ExceptionHandler.handleException(e);
		}
		return null;
	}
	
}
