/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2011/01/27
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
package org.jiemamy.eclipse.core.ui.editor.dialog.context.dataset;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;

import org.jiemamy.eclipse.core.ui.editor.dialog.AbstractTab;

/**
 * TODO for daisuke
 * 
 * @version $Id$
 * @author daisuke
 */
public class DescriptionTab extends AbstractTab {
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param parentTabFolder
	 * @param style
	 * @param tabTitle
	 */
	public DescriptionTab(TabFolder parentTabFolder, int style, String tabTitle) {
		super(parentTabFolder, style, tabTitle);
		
		Composite composite = new Composite(parentTabFolder, SWT.NULL);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label label = new Label(composite, SWT.WRAP | SWT.CENTER);
		label.setText(Messages.DataSetEditDialog_description);
		
		getTabItem().setControl(composite);
	}
	
	@Override
	public boolean isTabComplete() {
		return true;
	}
	
}
