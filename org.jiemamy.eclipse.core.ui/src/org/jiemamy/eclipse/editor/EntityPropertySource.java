/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
 * Created on 2009/03/10
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
package org.jiemamy.eclipse.editor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang.Validate;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import org.jiemamy.eclipse.utils.ExceptionHandler;
import org.jiemamy.utils.reflect.ReflectionUtil;

/**
 * {@link DatabaseObjectModel}を {@link IPropertySource}に適合させるためのアダプタクラス。
 * 
 * @author daisuke
 */
@Adapter(AdapterType.LOGIC)
public class EntityPropertySource implements IPropertySource {
	
	private final JiemamyFacade jiemamyFacade;
	
	private final DatabaseObjectModel entityModel;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param entityModel プロパティ表示対象のエンティティ
	 */
	public EntityPropertySource(DatabaseObjectModel entityModel) {
		Validate.notNull(entityModel);
		jiemamyFacade = entityModel.getJiemamy().getFactory().newFacade(JiemamyFacade.class);
		this.entityModel = entityModel;
	}
	
	public Object getEditableValue() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return new IPropertyDescriptor[] {
			new TextPropertyDescriptor(EntityProperty.name, "エンティティ名"), // RESOURCE
		};
	}
	
	public Object getPropertyValue(Object id) {
		if (id instanceof JiemamyProperty<?>) {
			@SuppressWarnings("unchecked")
			JiemamyProperty<DatabaseObjectModel> prop = (JiemamyProperty<DatabaseObjectModel>) id;
			Class<? extends DatabaseObjectModel> clazz = entityModel.getClass();
			try {
				String getterName = ReflectionUtil.convertFieldNameToAccessorName(prop.name(), ReflectionUtil.GET);
				Method m = clazz.getMethod(getterName);
				return m.invoke(entityModel);
			} catch (SecurityException e) {
				ExceptionHandler.handleException(e);
			} catch (IllegalArgumentException e) {
				ExceptionHandler.handleException(e);
			} catch (NoSuchMethodException e) {
				ExceptionHandler.handleException(e);
			} catch (IllegalAccessException e) {
				ExceptionHandler.handleException(e);
			} catch (InvocationTargetException e) {
				ExceptionHandler.handleException(e);
			}
		}
		return null;
	}
	
	public boolean isPropertySet(Object id) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void resetPropertyValue(Object id) {
		// resetできない
	}
	
	@SuppressWarnings("unchecked")
	public void setPropertyValue(Object id, Object value) {
		jiemamyFacade.changeModelProperty(entityModel, (JiemamyProperty<DatabaseObjectModel>) id, value);
	}
	
}
