/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2009/02/17
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
package org.jiemamy.eclipse.core.ui.editor.diagram.node.sticky;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.core.ui.Images;
import org.jiemamy.eclipse.core.ui.JiemamyUIPlugin;
import org.jiemamy.eclipse.core.ui.editor.diagram.JiemamyEditDialog0;
import org.jiemamy.eclipse.core.ui.editor.diagram.TextEditTab;
import org.jiemamy.eclipse.core.ui.utils.ConvertUtil;
import org.jiemamy.model.JmStickyNode;
import org.jiemamy.model.SimpleJmDiagram;

/** 
 * {@link JmStickyNode}の詳細編集ダイアログクラス。
 * 
 * @author daisuke
 */
public class StickyEditDialog extends JiemamyEditDialog0<JmStickyNode> {
	
	private static final Point DEFAULT_SIZE = new Point((int) (370 * 1.618), 370);
	
	private TextEditTab tabContents;
	
	private final SimpleJmDiagram diagram;
	

	/**
	 * コンストラクタ。
	 * 
	 * @param parentShell 親シェルオブジェクト
	 * @param context コンテキスト
	 * @param stickyNode 編集対象付箋モデル
	 * @param diagram ダイアグラム
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public StickyEditDialog(Shell parentShell, JiemamyContext context, JmStickyNode stickyNode, SimpleJmDiagram diagram) {
		super(parentShell, context, stickyNode, JmStickyNode.class);
		
		Validate.notNull(stickyNode);
		Validate.notNull(diagram);
		
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.diagram = diagram;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		final JmStickyNode stickyNode = getTargetCoreModel();
		getShell().setText(Messages.Dialog_Title);
		
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(6, false));
		
		// ---- A-1. 色
		ImageRegistry ir = JiemamyUIPlugin.getDefault().getImageRegistry();
		
		Button btnColor = new Button(composite, SWT.PUSH);
		btnColor.setImage(ir.get(Images.ICON_COLOR_PALETTE));
		btnColor.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent evt) {
				ColorDialog colorDialog = new ColorDialog(getShell(), SWT.NULL);
				RGB rgb = colorDialog.open();
				if (rgb != null) {
					stickyNode.setColor(ConvertUtil.convert(rgb));
					diagram.store(stickyNode);
					getContext().getFacet(DiagramFacet.class).store(diagram);
				}
			}
		});
		
		Button btnSimpleColor = new Button(composite, SWT.PUSH);
		btnSimpleColor.setText("default color"); // RESOURCE
		btnSimpleColor.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent evt) {
				stickyNode.setColor(null);
				diagram.store(stickyNode);
				getContext().getFacet(DiagramFacet.class).store(diagram);
			}
		});
		
		// ---- B. タブ
		TabFolder tabFolder = new TabFolder(composite, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 6;
		tabFolder.setLayoutData(gd);
		
		// ---- B-1. Contents
		String contents = StringUtils.defaultString(stickyNode.getContents());
		tabContents = new TextEditTab(tabFolder, Messages.Tab_Sticky_Contents, contents);
		tabContents.addKeyListener(new EditListenerImpl());
		addTab(tabContents);
		
		createAdditionalTabs(tabFolder);
		
		return composite;
	}
	
	@Override
	protected Point getDefaultSize() {
		return DEFAULT_SIZE;
	}
	
	@Override
	protected boolean performOk() {
		JmStickyNode stickyNode = getTargetCoreModel();
		
		String contents = StringUtils.defaultString(tabContents.getTextWidget().getText());
		stickyNode.setContents(contents);
		
		return true;
	}
}
