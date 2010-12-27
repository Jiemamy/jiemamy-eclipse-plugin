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

import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.ImageRegistry;
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
import org.jiemamy.dddbase.EntityRef;
import org.jiemamy.eclipse.Images;
import org.jiemamy.eclipse.JiemamyUIPlugin;
import org.jiemamy.eclipse.editor.dialog.AbstractEditListener;
import org.jiemamy.eclipse.editor.dialog.EditListener;
import org.jiemamy.eclipse.ui.AbstractTableEditor;
import org.jiemamy.eclipse.ui.DefaultTableEditorConfig;
import org.jiemamy.eclipse.ui.helper.TextSelectionAdapter;
import org.jiemamy.eclipse.ui.tab.AbstractTab;
import org.jiemamy.eclipse.utils.JiemamyPropertyUtil;
import org.jiemamy.model.attribute.ColumnModel;
import org.jiemamy.model.attribute.constraint.ConstraintModel;
import org.jiemamy.model.attribute.constraint.DefaultUniqueKeyConstraintModel;
import org.jiemamy.model.attribute.constraint.LocalKeyConstraintModel;
import org.jiemamy.model.attribute.constraint.PrimaryKeyConstraintModel;
import org.jiemamy.model.attribute.constraint.UniqueKeyConstraintModel;
import org.jiemamy.model.dbo.TableModel;
import org.jiemamy.transaction.Command;
import org.jiemamy.transaction.CommandListener;
import org.jiemamy.transaction.EventBroker;
import org.jiemamy.utils.LogMarker;
import org.jiemamy.utils.collection.CollectionsUtil;

/**
 * テーブル編集ダイアログの「キー制約」タブ。
 * 
 * @author daisuke
 */
public class TableEditDialogLocalKeyTab extends AbstractTab {
	
	private static Logger logger = LoggerFactory.getLogger(TableEditDialogLocalKeyTab.class);
	
	private final TableModel tableModel;
	
	private AbstractTableEditor localKeyTableEditor;
	
	/** モデル操作を行うファサード */
	private final JiemamyFacade jiemamyFacade;
	
	private final JiemamyContext context;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param parentTabFolder 親となるタブフォルダ
	 * @param style SWTスタイル値
	 * @param tableModel 編集対象テーブル
	 * @param jiemamyFacade モデル操作を行うファサード
	 */
	public TableEditDialogLocalKeyTab(TabFolder parentTabFolder, int style, JiemamyContext context,
			TableModel tableModel, JiemamyFacade jiemamyFacade) {
		super(parentTabFolder, style, Messages.Tab_Table_Keys);
		this.context = context;
		
		this.tableModel = tableModel;
		this.jiemamyFacade = jiemamyFacade;
		
		Composite composite = new Composite(parentTabFolder, SWT.NULL);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		localKeyTableEditor = new LocalKeyConstraintTableEditor(composite, SWT.NULL);
		localKeyTableEditor.configure();
		localKeyTableEditor.disableEditControls();
		
		getTabItem().setControl(composite);
	}
	
	@Override
	public boolean isTabComplete() {
		// TODO Auto-generated method stub
		return true;
	}
	

	/**
	 * 内部キー用ContentProvider実装クラス。
	 * 
	 * @author daisuke
	 */
	private class LocalKeyConstraintContentProvider extends ArrayContentProvider implements CommandListener {
		
		private Viewer viewer;
		

		public void commandExecuted(Command command) {
			logger.debug(LogMarker.LIFECYCLE, "LocalKeyConstraintContentProvider: commandExecuted");
			localKeyTableEditor.refreshTable(); // レコードの変更を反映させる。
		}
		
		@Override
		public void dispose() {
			logger.debug(LogMarker.LIFECYCLE, "LocalKeyConstraintContentProvider: dispose");
			super.dispose();
		}
		
		public JiemamyEntity getTargetModel() {
			return (JiemamyEntity) viewer.getInput();
		}
		
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			logger.debug(LogMarker.LIFECYCLE, "LocalKeyConstraintContentProvider: input changed");
			logger.trace(LogMarker.LIFECYCLE, "oldInput: " + oldInput);
			logger.trace(LogMarker.LIFECYCLE, "newInput: " + newInput);
			
			this.viewer = viewer;
			
			super.inputChanged(viewer, oldInput, newInput);
		}
		
	}
	
	/**
	 * キー制約用LabelProvider実装クラス。
	 * 
	 * @author daisuke
	 */
	private class LocalKeyConstraintLabelProvider extends BaseLabelProvider implements ITableLabelProvider {
		
		private LocalKeyConstraintLabelProvider() {
		}
		
		public Image getColumnImage(Object element, int columnIndex) {
			if ((element instanceof LocalKeyConstraintModel) == false) {
				return null;
			}
			
			LocalKeyConstraintModel localKey = (LocalKeyConstraintModel) element;
			if (columnIndex == 0 && localKey instanceof PrimaryKeyConstraintModel) {
				ImageRegistry ir = JiemamyUIPlugin.getDefault().getImageRegistry();
				return ir.get(Images.ICON_PK);
			} else {
				return null;
			}
		}
		
		public String getColumnText(Object element, int columnIndex) {
			if ((element instanceof LocalKeyConstraintModel) == false) {
				return StringUtils.EMPTY;
			}
			
			LocalKeyConstraintModel localKey = (LocalKeyConstraintModel) element;
			switch (columnIndex) {
				case 1:
					return localKey.getName();
					
				case 2:
					List<String> columnNames = CollectionsUtil.newArrayList();
					for (EntityRef<? extends ColumnModel> columnRef : localKey.getKeyColumns()) {
						columnNames.add(context.resolve(columnRef).getName());
					}
					return StringUtils.join(columnNames, ", ");
					
				default:
					return StringUtils.EMPTY;
			}
		}
	}
	
	private class LocalKeyConstraintTableEditor extends AbstractTableEditor {
		
		private static final int COL_WIDTH_NAME = 150;
		
		private static final int COL_WIDTH_COLUMNS = 400;
		
		private final EditListener editListener = new EditListenerImpl();
		
		private Text txtKeyConstraintName;
		
		private org.eclipse.swt.widgets.List lstKeyColumns;
		
		private List<LocalKeyConstraintModel> attributes;
		

		/**
		 * インスタンスを生成する。
		 * 
		 * @param parent 親コンポーネント
		 * @param style SWTスタイル値
		 */
		public LocalKeyConstraintTableEditor(Composite parent, int style) {
			super(parent, style, new DefaultTableEditorConfig("ローカルキー情報")); // RESOURCE
			
			attributes = tableModel.getConstraints(LocalKeyConstraintModel.class);
			
			assert attributes != null;
		}
		
		@Override
		protected void configureEditorControls() {
			super.configureEditorControls();
			
			txtKeyConstraintName.addFocusListener(new TextSelectionAdapter(txtKeyConstraintName));
			txtKeyConstraintName.addKeyListener(editListener);
			
			lstKeyColumns.addSelectionListener(editListener);
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
			tableViewer.setLabelProvider(new LocalKeyConstraintLabelProvider());
			final LocalKeyConstraintContentProvider contentProvider = new LocalKeyConstraintContentProvider();
			tableViewer.setContentProvider(contentProvider);
			tableViewer.setInput(attributes);
			tableViewer.addFilter(new ViewerFilter() {
				
				@Override
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					return element instanceof LocalKeyConstraintModel;
				}
				
			});
			
			final EventBroker eventBroker = context.getEventBroker();
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
			GridLayout layout = new GridLayout(2, false);
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			cmpNames.setLayout(layout);
			
			label = new Label(cmpNames, SWT.NULL);
			label.setText("制約名(&M)"); // RESOURCE
			
			txtKeyConstraintName = new Text(cmpNames, SWT.BORDER);
			txtKeyConstraintName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			label = new Label(cmpNames, SWT.NULL);
			label.setText("構成カラム(&O)"); // RESOURCE
			
			lstKeyColumns = new org.eclipse.swt.widgets.List(cmpNames, SWT.BORDER | SWT.MULTI);
			lstKeyColumns.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		
		@Override
		protected void createTableColumns(Table table) {
			TableColumn colMark = new TableColumn(table, SWT.LEFT);
			colMark.setText(StringUtils.EMPTY);
			colMark.setWidth(20);
			
			TableColumn colName = new TableColumn(table, SWT.LEFT);
			colName.setText("制約名"); // RESOURCE
			colName.setWidth(COL_WIDTH_NAME);
			
			TableColumn colColumns = new TableColumn(table, SWT.LEFT);
			colColumns.setText("構成カラム"); // RESOURCE
			colColumns.setWidth(COL_WIDTH_COLUMNS);
		}
		
		@Override
		protected void disableEditorControls() {
			txtKeyConstraintName.setText(StringUtils.EMPTY);
			lstKeyColumns.removeAll();
			
			txtKeyConstraintName.setEnabled(false);
			lstKeyColumns.setEnabled(false);
		}
		
		@Override
		protected void enableEditorControls(int index) {
			LocalKeyConstraintModel localKey = tableModel.getConstraints(LocalKeyConstraintModel.class).get(index);
			
			txtKeyConstraintName.setEnabled(true);
			lstKeyColumns.setEnabled(true);
			lstKeyColumns.removeAll();
			
			// 現在値の設定
			txtKeyConstraintName.setText(JiemamyPropertyUtil.careNull(localKey.getName()));
			List<EntityRef<? extends ColumnModel>> keyColumns = localKey.getKeyColumns();
			List<ColumnModel> columns = tableModel.getColumns();
			for (ColumnModel columnModel : columns) {
				lstKeyColumns.add(columnModel.getName());
				boolean found = false;
				for (EntityRef<? extends ColumnModel> columnRef : keyColumns) {
					if (columnRef.getReferentId().equals(columnModel.getId())) {
						found = true;
						break;
					}
				}
				if (found) {
					int[] newIndices =
							ArrayUtils.add(lstKeyColumns.getSelectionIndices(), columns.indexOf(columnModel));
					lstKeyColumns.setSelection(newIndices);
				}
			}
		}
		
		@Override
		protected UniqueKeyConstraintModel performAddItem() {
			Table table = getTableViewer().getTable();
			UniqueKeyConstraintModel uniqueKey = new DefaultUniqueKeyConstraintModel(null, null, null, null, null);
			
			jiemamyFacade.addAttribute(tableModel, uniqueKey);
			
			int addedIndex = tableModel.getConstraints(LocalKeyConstraintModel.class).indexOf(uniqueKey);
			table.setSelection(addedIndex);
			enableEditControls(addedIndex);
			txtKeyConstraintName.setFocus();
			
			return uniqueKey;
		}
		
		@Override
		protected UniqueKeyConstraintModel performInsertItem() {
			Table table = getTableViewer().getTable();
			int index = table.getSelectionIndex();
			
			UniqueKeyConstraintModel uniqueKey = new DefaultUniqueKeyConstraintModel(null, null, null, null, null);
			
			if (index < 0 || index > table.getItemCount()) {
				jiemamyFacade.addAttribute(tableModel, uniqueKey);
			} else {
				UniqueKeyConstraintModel attributeModel =
						(UniqueKeyConstraintModel) getTableViewer().getElementAt(index);
				int subjectIndex = attributes.indexOf(attributeModel);
				jiemamyFacade.addAttribute(tableModel, subjectIndex, uniqueKey);
			}
			
			int addedIndex = tableModel.getConstraints(LocalKeyConstraintModel.class).indexOf(uniqueKey);
			table.setSelection(addedIndex);
			enableEditControls(addedIndex);
			txtKeyConstraintName.setFocus();
			
			return uniqueKey;
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
			
			jiemamyFacade.swapListElement(tableModel, tableModel.getConstraints(), subjectIndex, objectIndex);
			
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
			
			int subjectIndex = tableModel.getConstraints().indexOf(subject);
			int objectIndex = tableModel.getConstraints().indexOf(object);
			
			jiemamyFacade.swapListElement(tableModel, tableModel.getConstraints(), subjectIndex, objectIndex);
			
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
			jiemamyFacade.removeAttribute(tableModel, (ConstraintModel) subject);
			
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
			
			LocalKeyConstraintModel localKey = tableModel.getConstraints(LocalKeyConstraintModel.class).get(editIndex);
			localKey.setName(JiemamyPropertyUtil.careNull(txtKeyConstraintName.getText(), true));
			List<EntityRef<? extends ColumnModel>> keyColumns = localKey.getKeyColumns();
			keyColumns.clear();
			for (int selectionIndex : lstKeyColumns.getSelectionIndices()) {
				ColumnModel columnModel = tableModel.getColumns().get(selectionIndex);
				keyColumns.add(columnModel.toReference());
			}
		}
		

		private class EditListenerImpl extends AbstractEditListener {
			
			@Override
			protected void process(TypedEvent e) {
				updateModel();
				localKeyTableEditor.refreshTable();
			}
		}
	}
}
