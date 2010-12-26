/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
 * Created on 2009/02/18
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
package org.jiemamy.eclipse.editor.dialog.table;

import java.util.SortedSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.JiemamyContext;
import org.jiemamy.JiemamyEntity;
import org.jiemamy.eclipse.editor.dialog.AbstractEditListener;
import org.jiemamy.eclipse.editor.dialog.EditListener;
import org.jiemamy.eclipse.ui.AbstractTableEditor;
import org.jiemamy.eclipse.ui.DefaultTableEditorConfig;
import org.jiemamy.eclipse.ui.helper.TextSelectionAdapter;
import org.jiemamy.eclipse.ui.tab.AbstractTab;
import org.jiemamy.model.attribute.constraint.CheckConstraintModel;
import org.jiemamy.model.attribute.constraint.ConstraintModel;
import org.jiemamy.model.attribute.constraint.DefaultCheckConstraintModel;
import org.jiemamy.model.dbo.TableModel;
import org.jiemamy.transaction.Command;
import org.jiemamy.transaction.CommandListener;
import org.jiemamy.transaction.EventBroker;
import org.jiemamy.utils.LogMarker;

/**
 * テーブル編集ダイアログの「チェック制約」タブ。
 * 
 * @author daisuke
 */
public class TableEditDialogCheckTab extends AbstractTab {
	
	private static Logger logger = LoggerFactory.getLogger(TableEditDialogCheckTab.class);
	
	private final TableModel tableModel;
	
	private AbstractTableEditor checkEditor;
	
	private final JiemamyFacade jiemamyFacade;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param parentTabFolder 親となるタブフォルダ
	 * @param style SWTスタイル値
	 * @param tableModel 編集対象テーブル
	 * @param jiemamyFacade モデル操作を行うファサード
	 */
	public TableEditDialogCheckTab(TabFolder parentTabFolder, int style, TableModel tableModel,
			JiemamyFacade jiemamyFacade) {
		super(parentTabFolder, style, "チェック制約(&H)"); // RESOURCE
		
		this.tableModel = tableModel;
		this.jiemamyFacade = jiemamyFacade;
		
		Composite composite = new Composite(parentTabFolder, SWT.NULL);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		checkEditor = new CheckTableEditor(composite, SWT.NULL);
		checkEditor.configure();
		checkEditor.disableEditControls();
		
		getTabItem().setControl(composite);
	}
	
	@Override
	public boolean isTabComplete() {
		// TODO Auto-generated method stub
		return true;
	}
	

	/**
	 * チェック制約用ContentProvider実装クラス。
	 * 
	 * @author daisuke
	 */
	private class CheckContentProvider extends ArrayContentProvider implements CommandListener {
		
		private Viewer viewer;
		

		public void commandExecuted(Command command) {
			logger.debug(LogMarker.LIFECYCLE, "CheckContentProvider: commandExecuted");
			checkEditor.refreshTable(); // レコードの変更を反映させる。
		}
		
		@Override
		public void dispose() {
			logger.debug(LogMarker.LIFECYCLE, "CheckContentProvider: disposed");
			super.dispose();
		}
		
		public JiemamyEntity getTargetModel() {
			return (JiemamyEntity) viewer.getInput();
		}
		
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			logger.debug(LogMarker.LIFECYCLE, "CheckContentProvider: input changed");
			logger.trace(LogMarker.LIFECYCLE, "oldInput: " + oldInput);
			logger.trace(LogMarker.LIFECYCLE, "newInput: " + newInput);
			
			this.viewer = viewer;
			
			super.inputChanged(viewer, oldInput, newInput);
		}
		
	}
	
	/**
	 * チェック制約用LabelProvider実装クラス。
	 * 
	 * @author daisuke
	 */
	private class CheckLabelProvider extends BaseLabelProvider implements ITableLabelProvider {
		
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		
		public String getColumnText(Object element, int columnIndex) {
			if ((element instanceof CheckConstraintModel) == false) {
				return StringUtils.EMPTY;
			}
			
			CheckConstraintModel check = (CheckConstraintModel) element;
			switch (columnIndex) {
				case 0:
					return check.getName();
					
				case 1:
					return check.getExpression();
					
				default:
					return StringUtils.EMPTY;
			}
		}
	}
	
	private class CheckTableEditor extends AbstractTableEditor {
		
		private static final int COL_WIDTH_NAME = 200;
		
		private static final int COL_WIDTH_EXPRESSION = 500;
		
		private final EditListener editListener = new EditListenerImpl();
		
		private final JiemamyContext jiemamy;
		
		private Text txtCheckName;
		
		private Text txtCheckExpression;
		
		private final SortedSet<? extends ConstraintModel> attributes;
		

		/**
		 * インスタンスを生成する。
		 * 
		 * @param parent 親コンポーネント
		 * @param style SWTスタイル値
		 */
		public CheckTableEditor(Composite parent, int style) {
			super(parent, style, new DefaultTableEditorConfig("チェック制約情報")); // RESOURCE
			
			jiemamy = tableModel.getJiemamy();
			attributes = tableModel.getConstraints();
			
			assert jiemamy != null;
			assert attributes != null;
		}
		
		@Override
		protected void configureEditorControls() {
			super.configureEditorControls();
			
			txtCheckName.addFocusListener(new TextSelectionAdapter(txtCheckName));
			txtCheckName.addKeyListener(editListener);
			
			txtCheckExpression.addFocusListener(new TextSelectionAdapter(txtCheckExpression));
			txtCheckExpression.addKeyListener(editListener);
		}
		
//		// THINK ↓要る？
//		@Override
//		protected void configureTable(final Table table) {
//			super.configureTable(table);
//			
//			final Menu menu = new Menu(table);
//			table.setMenu(menu);
//			menu.addMenuListener(new MenuAdapter() {
//				
//				@Override
//				public void menuShown(MenuEvent evt) {
//					for (MenuItem item : menu.getItems()) {
//						item.dispose();
//					}
//					int index = table.getSelectionIndex();
//					if (index == -1) {
//						return;
//					}
//					
//					MenuItem removeItem = new MenuItem(menu, SWT.PUSH);
//					removeItem.setText("&Remove"); // RESOURCE
//					removeItem.addSelectionListener(new SelectionAdapter() {
//						
//						@Override
//						public void widgetSelected(SelectionEvent evt) {
//							removeTableSelectionItem();
//						}
//					});
//				}
//			});
//		}
		
		@Override
		protected void configureTableViewer(TableViewer tableViewer) {
			tableViewer.setLabelProvider(new CheckLabelProvider());
			final CheckContentProvider contentProvider = new CheckContentProvider();
			tableViewer.setContentProvider(contentProvider);
			tableViewer.setInput(attributes);
			tableViewer.addFilter(new ViewerFilter() {
				
				@Override
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					return element instanceof CheckConstraintModel;
				}
				
			});
			
			final EventBroker eventBroker = jiemamy.getEventBroker();
			eventBroker.addListener(contentProvider);
			
			// THINK んーーー？？ このタイミングか？
			tableViewer.getTable().addDisposeListener(new DisposeListener() {
				
				public void widgetDisposed(DisposeEvent e) {
					eventBroker.removeListener(contentProvider);
				}
				
			});
		}
		
		@Override
		protected void createEditorControls(Composite parent) {
			Label label;
			
			Composite cmpNames = new Composite(parent, SWT.NULL);
			cmpNames.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			GridLayout layout = new GridLayout(4, false);
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			cmpNames.setLayout(layout);
			
			label = new Label(cmpNames, SWT.NULL);
			label.setText("制約名(&M)"); // RESOURCE
			
			txtCheckName = new Text(cmpNames, SWT.BORDER);
			txtCheckName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			label = new Label(cmpNames, SWT.NULL);
			label.setText("チェック制約式(&P)"); // RESOURCE
			
			txtCheckExpression = new Text(cmpNames, SWT.BORDER);
			txtCheckExpression.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		
		@Override
		protected void createTableColumns(Table table) {
			TableColumn colName = new TableColumn(table, SWT.LEFT);
			colName.setText("制約名"); // RESOURCE
			colName.setWidth(COL_WIDTH_NAME);
			
			TableColumn colExpression = new TableColumn(table, SWT.LEFT);
			colExpression.setText("チェック制約式"); // RESOURCE
			colExpression.setWidth(COL_WIDTH_EXPRESSION);
		}
		
		@Override
		protected void disableEditorControls() {
			txtCheckName.setEnabled(false);
			txtCheckExpression.setEnabled(false);
			
			txtCheckName.setText(StringUtils.EMPTY);
			txtCheckExpression.setText(StringUtils.EMPTY);
		}
		
		@Override
		protected void enableEditorControls(int index) {
			CheckConstraintModel checkConstraint = (CheckConstraintModel) getTableViewer().getElementAt(index);
			
			txtCheckName.setEnabled(true);
			txtCheckExpression.setEnabled(true);
			
			// 現在値の設定
			txtCheckName.setText(JiemamyPropertyUtil.careNull(checkConstraint.getName()));
			txtCheckExpression.setText(JiemamyPropertyUtil.careNull(checkConstraint.getExpression()));
		}
		
		@Override
		protected JiemamyEntity performAddItem() {
			Table table = getTableViewer().getTable();
			JiemamyFactory factory = jiemamy.getFactory();
			CheckConstraintModel checkConstraint = new DefaultCheckConstraintModel(null, null, null, null, null);
			
			jiemamyFacade.addAttribute(tableModel, checkConstraint);
			
			int addedIndex = tableModel.findAttributes(TableCheckConstraint.class).indexOf(checkConstraint);
			table.setSelection(addedIndex);
			enableEditControls(addedIndex);
			txtCheckName.setFocus();
			
			return checkConstraint;
		}
		
		@Override
		protected JiemamyEntity performInsertItem() {
			Table table = getTableViewer().getTable();
			int index = table.getSelectionIndex();
			
			JiemamyFactory factory = jiemamy.getFactory();
			TableCheckConstraint checkConstraint = factory.newModel(TableCheckConstraint.class);
			
			if (index < 0 || index > table.getItemCount()) {
				jiemamyFacade.addAttribute(tableModel, checkConstraint);
			} else {
				AttributeModel attributeModel = (AttributeModel) getTableViewer().getElementAt(index);
				int subjectIndex = attributes.indexOf(attributeModel);
				jiemamyFacade.addAttribute(tableModel, subjectIndex, checkConstraint);
			}
			
			int addedIndex = tableModel.findAttributes(TableCheckConstraint.class).indexOf(checkConstraint);
			table.setSelection(addedIndex);
			enableEditControls(addedIndex);
			txtCheckName.setFocus();
			
			return checkConstraint;
		}
		
		@Override
		protected void performMoveDownItem() {
			Table table = getTableViewer().getTable();
			int index = table.getSelectionIndex();
			if (index < 0 || index >= table.getItemCount()) {
				return;
			}
			
			Object subject = getTableViewer().getElementAt(index);
			Object object = getTableViewer().getElementAt(index + 1);
			
			int subjectIndex = tableModel.getConstraints().indexOf(subject);
			int objectIndex = tableModel.getConstraints().indexOf(object);
			
			jiemamyFacade.swapListElement(tableModel, tableModel.getAttributes(), subjectIndex, objectIndex);
			
			table.setSelection(index + 1);
			enableEditControls(index + 1);
		}
		
		@Override
		protected void performMoveUpItem() {
			Table table = getTableViewer().getTable();
			int index = table.getSelectionIndex();
			if (index <= 0 || index > table.getItemCount()) {
				return;
			}
			
			Object subject = getTableViewer().getElementAt(index);
			Object object = getTableViewer().getElementAt(index - 1);
			
			int subjectIndex = tableModel.getAttributes().indexOf(subject);
			int objectIndex = tableModel.getAttributes().indexOf(object);
			
			jiemamyFacade.swapListElement(tableModel, tableModel.getAttributes(), subjectIndex, objectIndex);
			
			table.setSelection(index - 1);
			enableEditControls(index - 1);
		}
		
		@Override
		protected JiemamyEntity performRemoveItem() {
			TableViewer tableViewer = getTableViewer();
			Table table = tableViewer.getTable();
			int index = table.getSelectionIndex();
			if (index < 0 || index > table.getItemCount()) {
				return null;
			}
			
			Object subject = getTableViewer().getElementAt(index);
			jiemamyFacade.removeAttribute(tableModel, (AttributeModel) subject);
			
			tableViewer.remove(subject);
			int nextSelection = table.getItemCount() > index ? index : index - 1;
			if (nextSelection >= 0) {
				table.setSelection(nextSelection);
				enableEditorControls(nextSelection);
			} else {
				disableEditorControls();
			}
			table.setFocus();
			
			return (JiemamyEntity) subject;
		}
		
		private void updateModel() {
			int editIndex = getTableViewer().getTable().getSelectionIndex();
			
			if (editIndex == -1) {
				return;
			}
			CheckConstraintModel checkConstraint = tableModel.getConstraints().get(editIndex);
			
			String checkName = JiemamyPropertyUtil.careNull(txtCheckName.getText(), true);
			jiemamyFacade.changeModelProperty(checkConstraint, ConstraintProperty.name, checkName);
			
			String expression = JiemamyPropertyUtil.careNull(txtCheckExpression.getText(), true);
			jiemamyFacade.changeModelProperty(checkConstraint, CheckConstraintProperty.expression, expression);
		}
		

		private class EditListenerImpl extends AbstractEditListener {
			
			@Override
			protected void process(TypedEvent e) {
				updateModel();
				checkEditor.refreshTable();
			}
		}
	}
}
