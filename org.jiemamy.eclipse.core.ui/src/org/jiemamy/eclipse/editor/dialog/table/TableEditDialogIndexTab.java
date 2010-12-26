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

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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
import org.jiemamy.eclipse.editor.dialog.AbstractEditListener;
import org.jiemamy.eclipse.editor.dialog.EditListener;
import org.jiemamy.eclipse.ui.AbstractTableEditor;
import org.jiemamy.eclipse.ui.DefaultTableEditorConfig;
import org.jiemamy.eclipse.ui.helper.TextSelectionAdapter;
import org.jiemamy.eclipse.ui.tab.AbstractTab;
import org.jiemamy.model.attribute.ColumnModel;
import org.jiemamy.model.dbo.TableModel;
import org.jiemamy.model.dbo.index.IndexColumnModel;
import org.jiemamy.model.dbo.index.IndexColumnModel.SortOrder;
import org.jiemamy.model.dbo.index.IndexModel;
import org.jiemamy.transaction.Command;
import org.jiemamy.transaction.CommandListener;
import org.jiemamy.transaction.EventBroker;
import org.jiemamy.utils.LogMarker;

/**
 * テーブル編集ダイアログの「インデックス」タブ
 * 
 * @author daisuke
 */
public class TableEditDialogIndexTab extends AbstractTab {
	
	private static Logger logger = LoggerFactory.getLogger(TableEditDialogIndexTab.class);
	
	private final TableModel tableModel;
	
	private IndexTableEditor indexesTableEditor;
	
	private IndexColumnTableEditor indexColumnsTableEditor;
	
	private final JiemamyFacade jiemamyFacade;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param parent 親となるタブフォルダ
	 * @param style SWTスタイル値
	 * @param tableModel 編集対象テーブル
	 * @param jiemamyFacade モデル操作を行うファサード
	 */
	public TableEditDialogIndexTab(TabFolder parent, int style, TableModel tableModel, JiemamyFacade jiemamyFacade) {
		super(parent, style, "インデックス(&I)"); // RESOURCE
		
		this.tableModel = tableModel;
		this.jiemamyFacade = jiemamyFacade;
		
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		indexesTableEditor = new IndexTableEditor(composite, SWT.NULL);
		indexesTableEditor.configure();
		indexesTableEditor.disableEditControls();
		
		indexColumnsTableEditor = new IndexColumnTableEditor(composite, SWT.NULL);
		indexColumnsTableEditor.configure();
		indexColumnsTableEditor.disableEditControls();
		
		getTabItem().setControl(composite);
	}
	
	@Override
	public boolean isTabComplete() {
		// TODO Auto-generated method stub
		return true;
	}
	

	/**
	 * インデックスカラム用ContentProvider実装クラス。
	 * 
	 * @author daisuke
	 */
	private class IndexColumnContentProvider extends ArrayContentProvider implements CommandListener {
		
		private Viewer viewer;
		

		public void commandExecuted(Command command) {
			logger.debug(LogMarker.LIFECYCLE, "IndexColumnContentProvider: commandExecuted");
			indexColumnsTableEditor.refreshTable(); // インデックスカラムの変更を反映させる。
		}
		
		@Override
		public void dispose() {
			logger.debug(LogMarker.LIFECYCLE, "IndexColumnContentProvider: dispose");
		}
		
		public JiemamyEntity getTargetModel() {
			return (JiemamyEntity) viewer.getInput();
		}
		
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			logger.debug(LogMarker.LIFECYCLE, "IndexColumnContentProvider: input changed");
			logger.trace(LogMarker.LIFECYCLE, "oldInput: " + oldInput);
			logger.trace(LogMarker.LIFECYCLE, "newInput: " + newInput);
			
			this.viewer = viewer;
			indexColumnsTableEditor.refreshTable();
		}
	}
	
	/**
	 * インデックスカラム用LabelProvider実装クラス。
	 * 
	 * @author daisuke
	 */
	private class IndexColumnLabelProvider extends BaseLabelProvider implements ITableLabelProvider {
		
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		
		public String getColumnText(Object element, int columnIndex) {
			IndexColumnModel indexColumnModel = (IndexColumnModel) element;
			ReferenceResolver referenceResolver = indexColumnModel.getJiemamy().getReferenceResolver();
			switch (columnIndex) {
				case 0:
					EntityRef<? extends ColumnModel> columnRef = indexColumnModel.getColumnRef();
					ColumnModel columnModel = referenceResolver.resolve(columnRef);
					return columnModel.getName();
				case 1:
					SortOrder sortOrder = indexColumnModel.getSortOrder();
					return ObjectUtils.toString(sortOrder);
					
				default:
					return StringUtils.EMPTY;
			}
		}
	}
	
	private class IndexColumnTableEditor extends AbstractTableEditor {
		
		private static final int COL_WIDTH_NAME = 180;
		
		private static final int COL_WIDTH_SORT_ORDER = 80;
		
		private final EditListener editListener = new IndexColumnEditListenerImpl();
		
		private final JiemamyContext jiemamy;
		
		private Button radSortNone;
		
		private Button radSortAsc;
		
		private Button radSortDesc;
		
		private List<IndexColumnModel> indexColumns;
		

		/**
		 * インスタンスを生成する。
		 * 
		 * @param parent 親コンポーネント
		 * @param style SWTスタイル値
		 */
		public IndexColumnTableEditor(Composite parent, int style) {
			super(parent, style, new DefaultTableEditorConfig("インデックスカラム情報") {
				
				@Override
				public String getAddLabel() {
					return "追加(&D)"; // RESOURCE
				}
				
				@Override
				public String getInsertLabel() {
					return "挿入(&S)"; // RESOURCE
				}
				
				@Override
				public String getRemoveLabel() {
					return "削除(&E)"; // RESOURCE
				}
				
			}); // RESOURCE
			
			jiemamy = tableModel.getJiemamy();
		}
		
		public void updateInput() {
			TableViewer tableViewer = getTableViewer();
			int index = indexesTableEditor.getTableViewer().getTable().getSelectionIndex();
			if (index >= 0) {
				indexColumns = tableModel.getIndexes().get(index).getIndexColumns();
				tableViewer.setInput(indexColumns);
				indexColumnsTableEditor.getBtnAdd().setEnabled(true);
			} else {
				tableViewer.setInput(null);
				indexColumnsTableEditor.getBtnAdd().setEnabled(false);
			}
		}
		
		@Override
		protected void configureEditorControls() {
			super.configureEditorControls();
			
			radSortNone.addSelectionListener(editListener);
			radSortAsc.addSelectionListener(editListener);
			radSortDesc.addSelectionListener(editListener);
		}
		
		@Override
		protected void configureTableViewer(TableViewer tableViewer) {
			tableViewer.setLabelProvider(new IndexColumnLabelProvider());
			final IndexColumnContentProvider contentProvider = new IndexColumnContentProvider();
			tableViewer.setContentProvider(contentProvider);
			
			updateInput();
			
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
			Composite cmpRadio = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout(3, false);
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			cmpRadio.setLayout(layout);
			cmpRadio.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			radSortNone = new Button(cmpRadio, SWT.RADIO);
			radSortNone.setText("なし(&O)"); // RESOURCE
			
			radSortAsc = new Button(cmpRadio, SWT.RADIO);
			radSortAsc.setText("昇順(&A)"); // RESOURCE
			
			radSortDesc = new Button(cmpRadio, SWT.RADIO);
			radSortDesc.setText("降順(&E)"); // RESOURCE
		}
		
		@Override
		protected void createTableColumns(Table table) {
			TableColumn colColumn = new TableColumn(table, SWT.LEFT);
			colColumn.setText("カラム名"); // RESOURCE
			colColumn.setWidth(COL_WIDTH_NAME);
			
			TableColumn colSort = new TableColumn(table, SWT.LEFT);
			colSort.setText("ソート順"); // RESOURCE
			colSort.setWidth(COL_WIDTH_SORT_ORDER);
		}
		
		@Override
		protected void disableEditButtons() {
			if (getBtnAdd() != null) {
				getBtnAdd().setEnabled(false);
			}
			super.disableEditButtons();
		}
		
		@Override
		protected void disableEditorControls() {
			radSortNone.setEnabled(false);
			radSortAsc.setEnabled(false);
			radSortDesc.setEnabled(false);
			
			radSortNone.setSelection(false);
			radSortAsc.setSelection(false);
			radSortDesc.setSelection(false);
		}
		
		@Override
		protected void enableEditButtons(int index) {
			if (getBtnAdd() != null) {
				getBtnAdd().setEnabled(true);
			}
			super.enableEditButtons(index);
		}
		
		@Override
		protected void enableEditorControls(int index) {
			int indexIndex = indexesTableEditor.getTableViewer().getTable().getSelectionIndex();
			
			IndexModel indexModel = tableModel.getIndexes().get(indexIndex);
			IndexColumnModel indexColumnModel = indexModel.getIndexColumns().get(index);
			
			radSortNone.setEnabled(true);
			radSortAsc.setEnabled(true);
			radSortDesc.setEnabled(true);
			
			radSortNone.setSelection(false);
			radSortAsc.setSelection(false);
			radSortDesc.setSelection(false);
			if (SortOrder.ASC.equals(indexColumnModel.getSortOrder())) {
				radSortAsc.setSelection(true);
			} else if (SortOrder.DESC.equals(indexColumnModel.getSortOrder())) {
				radSortDesc.setSelection(true);
			} else {
				radSortNone.setSelection(true);
			}
		}
		
		@Override
		protected JiemamyEntity performAddItem() {
			Table table = getTableViewer().getTable();
			int indexIndex = indexesTableEditor.getTableViewer().getTable().getSelectionIndex();
			
			List<ColumnModel> columns = tableModel.getColumns();
			ColumnSelectDialog dialog = new ColumnSelectDialog(table.getShell(), columns);
			
			if (dialog.open() == Dialog.OK && dialog.getResult() != null && indexIndex != -1) {
				JiemamyFactory factory = jiemamy.getFactory();
				IndexColumnModel indexColumnModel = factory.newModel(IndexColumnModel.class);
				
				EntityRef<? extends ColumnModel> newColumnRef = factory.newReference(dialog.getResult());
				jiemamyFacade.changeModelProperty(indexColumnModel, IndexColumnProperty.columnRef, newColumnRef);
				
				IndexModel indexModel = tableModel.getIndexes().get(indexIndex);
				jiemamyFacade.addIndexColumn(indexModel, indexColumnModel);
				
				int addedIndex = tableModel.getIndexes().get(indexIndex).getIndexColumns().indexOf(indexColumnModel);
				table.setSelection(addedIndex);
				enableEditControls(addedIndex);
				
				indexColumnsTableEditor.refreshTable();
				return indexColumnModel;
			}
			return null;
		}
		
		@Override
		protected JiemamyEntity performInsertItem() {
			Table table = getTableViewer().getTable();
			int index = table.getSelectionIndex();
			int indexIndex = indexesTableEditor.getTableViewer().getTable().getSelectionIndex();
			List<ColumnModel> columns = tableModel.getColumns();
			ColumnSelectDialog dialog = new ColumnSelectDialog(table.getShell(), columns);
			
			if (dialog.open() == Dialog.OK && dialog.getResult() != null && indexIndex != -1) {
				JiemamyFactory factory = jiemamy.getFactory();
				IndexColumnModel indexColumnModel = factory.newModel(IndexColumnModel.class);
				
				EntityRef<? extends ColumnModel> columnRef = factory.newReference(dialog.getResult());
				jiemamyFacade.changeModelProperty(indexColumnModel, IndexColumnProperty.columnRef, columnRef);
				
				IndexModel indexModel = tableModel.getIndexes().get(indexIndex);
				if (index < 0 || index > table.getItemCount()) {
					jiemamyFacade.addIndexColumn(indexModel, indexColumnModel);
				} else {
					jiemamyFacade.addIndexColumn(indexModel, index, indexColumnModel);
				}
				
				int addedIndex = tableModel.getIndexes().get(indexIndex).getIndexColumns().indexOf(indexColumnModel);
				table.setSelection(addedIndex);
				enableEditControls(addedIndex);
				
				indexColumnsTableEditor.refreshTable();
				return indexColumnModel;
			}
			return null;
		}
		
		@Override
		protected void performMoveDownItem() {
			Table table = getTableViewer().getTable();
			int index = table.getSelectionIndex();
			int indexIndex = indexesTableEditor.getTableViewer().getTable().getSelectionIndex();
			if (index < 0 || index >= table.getItemCount()) {
				return;
			}
			
			IndexModel indexModel = tableModel.getIndexes().get(indexIndex);
			jiemamyFacade.swapListElement(indexModel, indexColumns, index, index + 1);
			
			indexColumnsTableEditor.refreshTable();
			table.setSelection(index + 1);
			enableEditControls(index + 1);
		}
		
		@Override
		protected void performMoveUpItem() {
			Table table = getTableViewer().getTable();
			int index = table.getSelectionIndex();
			int indexIndex = indexesTableEditor.getTableViewer().getTable().getSelectionIndex();
			if (index <= 0 || index > table.getItemCount()) {
				return;
			}
			
			IndexModel indexModel = tableModel.getIndexes().get(indexIndex);
			jiemamyFacade.swapListElement(indexModel, indexColumns, index, index - 1);
			
			indexColumnsTableEditor.refreshTable();
			table.setSelection(index - 1);
			enableEditControls(index - 1);
		}
		
		@Override
		protected JiemamyEntity performRemoveItem() {
			TableViewer tableViewer = getTableViewer();
			Table table = tableViewer.getTable();
			int index = table.getSelectionIndex();
			int indexIndex = indexesTableEditor.getTableViewer().getTable().getSelectionIndex();
			if (index < 0 || index > table.getItemCount()) {
				return null;
			}
			IndexColumnModel removed = tableModel.getIndexes().get(indexIndex).getIndexColumns().remove(index);
			
			tableViewer.remove(index);
			int nextSelection = table.getItemCount() > index ? index : index - 1;
			if (nextSelection >= 0) {
				table.setSelection(nextSelection);
				enableEditorControls(nextSelection);
			} else {
				disableEditorControls();
			}
			table.setFocus();
			
			indexColumnsTableEditor.refreshTable();
			return removed;
		}
		
		private void updateModel() {
			Table table = getTableViewer().getTable();
			int index = table.getSelectionIndex();
			int indexIndex = indexesTableEditor.getTableViewer().getTable().getSelectionIndex();
			
			if (indexIndex != -1 && index != -1) {
				IndexColumnModel indexColumnModel =
						tableModel.getIndexes().get(indexIndex).getIndexColumns().get(index);
				// UNDONE indexColumnModel.getColumn(). addとかremoveとか
				
				SortOrder sortOrder = null;
				if (radSortAsc.getSelection()) {
					sortOrder = SortOrder.ASC;
				} else if (radSortDesc.getSelection()) {
					sortOrder = SortOrder.DESC;
				}
				jiemamyFacade.changeModelProperty(indexColumnModel, IndexColumnProperty.sortOrder, sortOrder);
			}
		}
		

		private class IndexColumnEditListenerImpl extends AbstractEditListener {
			
			@Override
			protected void process(TypedEvent e) {
				updateModel();
			}
		}
	}
	
	/**
	 * インデックス用ContentProvider実装クラス。
	 * 
	 * @author daisuke
	 */
	private class IndexContentProvider extends ArrayContentProvider implements CommandListener {
		
		private Viewer viewer;
		

		public void commandExecuted(Command command) {
			logger.debug(LogMarker.LIFECYCLE, "IndexContentProvider: commandExecuted");
			indexesTableEditor.refreshTable(); // インデックスの変更を反映させる。
		}
		
		@Override
		public void dispose() {
			logger.debug(LogMarker.LIFECYCLE, "IndexContentProvider: disposed");
			super.dispose();
		}
		
		public JiemamyEntity getTargetModel() {
			return (JiemamyEntity) viewer.getInput();
		}
		
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			logger.debug(LogMarker.LIFECYCLE, "IndexContentProvider: input changed");
			logger.trace(LogMarker.LIFECYCLE, "oldInput: " + oldInput);
			logger.trace(LogMarker.LIFECYCLE, "newInput: " + newInput);
			
			this.viewer = viewer;
			
			super.inputChanged(viewer, oldInput, newInput);
		}
	}
	
	/**
	 * インデックス用LabelProvider実装クラス。
	 * 
	 * @author daisuke
	 */
	private class IndexLabelProvider extends BaseLabelProvider implements ITableLabelProvider {
		
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		
		public String getColumnText(Object element, int columnIndex) {
			IndexModel indexModel = (IndexModel) element;
			switch (columnIndex) {
				case 0:
					return indexModel.getName();
				case 1:
					return String.valueOf(indexModel.isUnique());
					
				default:
					return StringUtils.EMPTY;
			}
		}
	}
	
	private class IndexTableEditor extends AbstractTableEditor {
		
		/** 名前カラムの幅 */
		private static final int COL_WIDTH_NAME = 180;
		
		/** 一意カラムの幅 */
		private static final int COL_WIDTH_UNIQUE = 50;
		
		private final EditListener editListener = new IndexEditListenerImpl();
		
		private final JiemamyContext jiemamy;
		
		private Text txtIndexName;
		
		private Button chkIsUniqueIndex;
		
		private final List<IndexModel> indexes;
		

		/**
		 * インスタンスを生成する。
		 * 
		 * @param parent 親コンポーネント
		 * @param style SWTスタイル値
		 */
		public IndexTableEditor(Composite parent, int style) {
			super(parent, style, new DefaultTableEditorConfig("インデックス情報")); // RESOURCE
			
			jiemamy = tableModel.getJiemamy();
			indexes = tableModel.getIndexes();
			
			assert jiemamy != null;
			assert indexes != null;
		}
		
		@Override
		protected void configureEditorControls() {
			super.configureEditorControls();
			
			txtIndexName.addFocusListener(new TextSelectionAdapter(txtIndexName));
			txtIndexName.addKeyListener(editListener);
			
			chkIsUniqueIndex.addSelectionListener(editListener);
		}
		
		@Override
		protected void configureTable(final Table table) {
			super.configureTable(table);
			
			table.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					onTableRowSelected(table.getSelectionIndex());
				}
			});
		}
		
		@Override
		protected void configureTableViewer(TableViewer tableViewer) {
			tableViewer.setLabelProvider(new IndexLabelProvider());
			final IndexContentProvider contentProvider = new IndexContentProvider();
			tableViewer.setContentProvider(contentProvider);
			tableViewer.setInput(indexes);
			
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
			Composite cmpNames = new Composite(parent, SWT.NULL);
			cmpNames.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			GridLayout layout = new GridLayout(3, false);
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			cmpNames.setLayout(layout);
			
			Label label = new Label(cmpNames, SWT.NULL);
			label.setText("インデックス名(&M)"); // RESOURCE
			
			txtIndexName = new Text(cmpNames, SWT.BORDER);
			txtIndexName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			chkIsUniqueIndex = new Button(parent, SWT.CHECK);
			chkIsUniqueIndex.setText("一意(&U)"); // RESOURCE
		}
		
		@Override
		protected void createTableColumns(Table table) {
			TableColumn colName = new TableColumn(table, SWT.LEFT);
			colName.setText("インデックス名"); // RESOURCE
			colName.setWidth(COL_WIDTH_NAME);
			
			TableColumn colUnique = new TableColumn(table, SWT.LEFT);
			colUnique.setText("一意"); // RESOURCE
			colUnique.setWidth(COL_WIDTH_UNIQUE);
		}
		
		@Override
		protected void disableEditorControls() {
			txtIndexName.setEnabled(false);
			chkIsUniqueIndex.setEnabled(false);
			
			txtIndexName.setText(StringUtils.EMPTY);
			chkIsUniqueIndex.setSelection(false);
		}
		
		@Override
		protected void enableEditorControls(int index) {
			IndexModel indexModel = indexes.get(index);
			
			txtIndexName.setEnabled(true);
			chkIsUniqueIndex.setEnabled(true);
			
			// 現在値の設定
			txtIndexName.setText(JiemamyPropertyUtil.careNull(indexModel.getName()));
			chkIsUniqueIndex.setSelection(indexModel.isUnique());
		}
		
		@Override
		protected JiemamyEntity performAddItem() {
			Table table = getTableViewer().getTable();
			JiemamyFactory factory = jiemamy.getFactory();
			IndexModel indexModel = factory.newModel(IndexModel.class);
			
			String newName = "idx_" + tableModel.getName() + "_" + (tableModel.getIndexes().size() + 1);
			jiemamyFacade.changeModelProperty(indexModel, IndexProperty.name, newName);
			
			jiemamyFacade.addIndex(tableModel, indexModel);
			
			int addedIndex = tableModel.getIndexes().indexOf(indexModel);
			table.setSelection(addedIndex);
			onTableRowSelected(addedIndex);
			enableEditControls(addedIndex);
			txtIndexName.setFocus();
			
			return indexModel;
		}
		
		@Override
		protected JiemamyEntity performInsertItem() {
			Table table = getTableViewer().getTable();
			int index = table.getSelectionIndex();
			
			JiemamyFactory factory = jiemamy.getFactory();
			IndexModel indexModel = factory.newModel(IndexModel.class);
			
			String newName = "idx_" + tableModel.getName() + "_" + (tableModel.getIndexes().size() + 1);
			jiemamyFacade.changeModelProperty(indexModel, IndexProperty.name, newName);
			
			if (index < 0 || index > table.getItemCount()) {
				jiemamyFacade.addIndex(tableModel, indexModel);
			} else {
				jiemamyFacade.addIndex(tableModel, index, indexModel);
			}
			
			int addedIndex = tableModel.getIndexes().indexOf(indexModel);
			table.setSelection(addedIndex);
			onTableRowSelected(addedIndex);
			enableEditControls(addedIndex);
			txtIndexName.setFocus();
			
			return indexModel;
		}
		
		@Override
		protected void performMoveDownItem() {
			Table table = getTableViewer().getTable();
			int index = table.getSelectionIndex();
			if (index < 0 || index >= table.getItemCount()) {
				return;
			}
			
			jiemamyFacade.swapListElement(tableModel, indexes, index, index + 1);
			
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
			
			jiemamyFacade.swapListElement(tableModel, indexes, index, index - 1);
			
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
			
			IndexModel indexModel = indexes.get(index);
			jiemamyFacade.removeIndex(tableModel, indexModel);
			
			tableViewer.remove(indexModel);
			int nextSelection = table.getItemCount() > index ? index : index - 1;
			if (nextSelection >= 0) {
				table.setSelection(nextSelection);
				onTableRowSelected(nextSelection);
				enableEditorControls(nextSelection);
			} else {
				disableEditorControls();
			}
			table.setFocus();
			
			return indexModel;
		}
		
		private void onTableRowSelected(int index) {
			if (index >= 0) {
				indexColumnsTableEditor.updateInput();
			} else {
				indexColumnsTableEditor.disableEditControls();
			}
		}
		
		private void updateModel() {
			Table table = getTableViewer().getTable();
			int index = table.getSelectionIndex();
			if (index == -1) {
				return;
			}
			IndexModel indexModel = tableModel.getIndexes().get(index);
			
			String indexName = JiemamyPropertyUtil.careNull(txtIndexName.getText(), true);
			jiemamyFacade.changeModelProperty(indexModel, IndexProperty.name, indexName);
			
			boolean uniqueIndex = chkIsUniqueIndex.getSelection();
			jiemamyFacade.changeModelProperty(indexModel, IndexProperty.unique, uniqueIndex);
		}
		

		private class IndexEditListenerImpl extends AbstractEditListener {
			
			@Override
			protected void process(TypedEvent e) {
				updateModel();
				indexesTableEditor.refreshTable();
			}
		}
	}
}
