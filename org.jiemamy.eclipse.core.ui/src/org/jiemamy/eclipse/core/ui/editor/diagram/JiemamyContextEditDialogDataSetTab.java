/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.core.ui.editor.diagram;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
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
import org.jiemamy.eclipse.core.ui.editor.diagram.dataset.DataSetEditDialog;
import org.jiemamy.eclipse.core.ui.utils.TextSelectionAdapter;
import org.jiemamy.model.dataset.JmDataSet;
import org.jiemamy.model.dataset.SimpleJmDataSet;
import org.jiemamy.transaction.EventBroker;
import org.jiemamy.transaction.StoredEvent;
import org.jiemamy.transaction.StoredEventListener;
import org.jiemamy.utils.LogMarker;

/** 
 * データベース編集ダイアログの「データセット」タブ。
 * 
 * @author daisuke
 */
public class JiemamyContextEditDialogDataSetTab extends AbstractTab {
	
	private static Logger logger = LoggerFactory.getLogger(JiemamyContextEditDialogDataSetTab.class);
	
	private final JiemamyContext context;
	
	private AbstractTableEditor dataSetTableEditor;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param parentTabFolder 親となるタブフォルダ
	 * @param style SWTスタイル値
	 * @param context 編集対象{@link JiemamyContext}
	 */
	public JiemamyContextEditDialogDataSetTab(TabFolder parentTabFolder, int style, JiemamyContext context) {
		super(parentTabFolder, style, "データセット(&T)"); // RESOURCE
		Validate.notNull(context);
		
		this.context = context;
		
		Composite composite = new Composite(parentTabFolder, SWT.NULL);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		dataSetTableEditor = new DataSetTableEditor(composite, SWT.NULL);
		dataSetTableEditor.configure();
		dataSetTableEditor.disableEditControls();
		
		getTabItem().setControl(composite);
	}
	
	@Override
	public boolean isTabComplete() {
		return true;
	}
	

	/**
	 * DataSet用ContentProvider実装クラス。
	 * 
	 * @author daisuke
	 */
	private class DataSetContentProvider implements IStructuredContentProvider, StoredEventListener {
		
		public void commandExecuted(StoredEvent<?> command) {
			dataSetTableEditor.refreshTable(); // レコードの変更を反映させる。
		}
		
		public void dispose() {
			// nothing to do
		}
		
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof JiemamyContext) {
				JiemamyContext context = (JiemamyContext) inputElement;
				return context.getDataSets().toArray();
			}
			logger.error("unknown input: " + inputElement.getClass().getName());
			return new Object[0];
		}
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			logger.debug(LogMarker.LIFECYCLE, "DataSetContentProvider: input changed");
			logger.trace(LogMarker.LIFECYCLE, "oldInput: " + oldInput);
			logger.trace(LogMarker.LIFECYCLE, "newInput: " + newInput);
		}
	}
	
	/**
	 * DataSet用 {@link LabelProvider} 実装クラス。
	 * 
	 * @author daisuke
	 */
	private class DataSetLabelProvider extends BaseLabelProvider implements ITableLabelProvider {
		
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		
		public String getColumnText(Object element, int columnIndex) {
			JmDataSet dataSet = (JmDataSet) element;
			switch (columnIndex) {
				case 0:
					return dataSet.getName();
					
				default:
					return StringUtils.EMPTY;
			}
		}
	}
	
	private class DataSetTableEditor extends AbstractTableEditor {
		
		private static final int COL_WIDTH_NAME = 200;
		
		private final EditListener editListener = new EditListenerImpl();
		
		private Text txtDataSetName;
		
		private Button btnEdit;
		

		/**
		 * インスタンスを生成する。
		 * 
		 * @param parent 親コンポーネント
		 * @param style SWTスタイル値
		 */
		public DataSetTableEditor(Composite parent, int style) {
			super(parent, style, new DefaultTableEditorConfig("データセット情報")); // RESOURCE
		}
		
		@Override
		protected void configureEditorControls() {
			super.configureEditorControls();
			
			txtDataSetName.addFocusListener(new TextSelectionAdapter(txtDataSetName));
			txtDataSetName.addKeyListener(editListener);
			
			btnEdit.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					logger.info("edit data set");
					int selectionIndex = dataSetTableEditor.getTableViewer().getTable().getSelectionIndex();
					SimpleJmDataSet dataSetModel = (SimpleJmDataSet) context.getDataSets().get(selectionIndex);
					DataSetEditDialog dataSetEditDialog = new DataSetEditDialog(getShell(), context, dataSetModel);
					if (dataSetEditDialog.open() == Window.OK) {
						context.store(dataSetModel);
					}
				}
			});
		}
		
		@Override
		protected void configureTableViewer(TableViewer tableViewer) {
			tableViewer.setLabelProvider(new DataSetLabelProvider());
			final DataSetContentProvider contentProvider = new DataSetContentProvider();
			tableViewer.setContentProvider(contentProvider);
			tableViewer.setInput(context);
			
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
			GridLayout layout = new GridLayout(3, false);
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			cmpNames.setLayout(layout);
			
			label = new Label(cmpNames, SWT.NULL);
			label.setText("データセット名"); // RESOURCE
			
			txtDataSetName = new Text(cmpNames, SWT.BORDER);
			txtDataSetName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			btnEdit = new Button(cmpNames, SWT.PUSH);
			btnEdit.setText("編集"); // RESOURCE
		}
		
		@Override
		protected void createTableColumns(Table table) {
			TableColumn colName = new TableColumn(table, SWT.LEFT);
			colName.setText("データセット名"); // RESOURCE
			colName.setWidth(COL_WIDTH_NAME);
		}
		
		@Override
		protected void disableEditorControls() {
			txtDataSetName.setText(StringUtils.EMPTY);
			txtDataSetName.setEnabled(false);
			
			btnEdit.setEnabled(false);
		}
		
		@Override
		protected void enableEditorControls(int index) {
			SimpleJmDataSet dataSet = (SimpleJmDataSet) getTableViewer().getElementAt(index);
			
			txtDataSetName.setEnabled(true);
			btnEdit.setEnabled(true);
			
			// 現在値の設定
			txtDataSetName.setText(StringUtils.defaultString(dataSet.getName()));
		}
		
		@Override
		protected void performAddItem() {
			Table table = getTableViewer().getTable();
			
			SimpleJmDataSet dataSetModel = new SimpleJmDataSet(UUID.randomUUID());
			
			String newName = "DATASET_" + (context.getDataSets().size() + 1);
			dataSetModel.setName(newName);
			
			context.store(dataSetModel);
			
			int addedIndex = context.getDataSets().indexOf(dataSetModel);
			table.setSelection(addedIndex);
			enableEditControls(addedIndex);
			txtDataSetName.setFocus();
		}
		
		@Override
		protected void performInsertItem() {
			Table table = getTableViewer().getTable();
			int index = table.getSelectionIndex();
			
			SimpleJmDataSet dataSetModel = new SimpleJmDataSet(UUID.randomUUID());
			
			String newName = "DATASET_" + (context.getDataSets().size() + 1);
			dataSetModel.setName(newName);
			
			dataSetModel.setIndex(index);
			context.store(dataSetModel);
			
			int addedIndex = context.getDataSets().indexOf(dataSetModel);
			table.setSelection(addedIndex);
			enableEditControls(addedIndex);
			txtDataSetName.setFocus();
		}
		
		@Override
		protected void performMoveDownItem() {
			Table table = getTableViewer().getTable();
			int index = table.getSelectionIndex();
			if (index < 0 || index >= table.getItemCount()) {
				return;
			}
			
			context.swapDataSet(index, index + 1);
			
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
			
			context.swapDataSet(index - 1, index);
			
			table.setSelection(index - 1);
			enableEditControls(index - 1);
		}
		
		@Override
		protected void performRemoveItem() {
			TableViewer tableViewer = getTableViewer();
			Table table = tableViewer.getTable();
			int index = table.getSelectionIndex();
			if (index < 0 || index > table.getItemCount()) {
				return;
			}
			
			JmDataSet subject = (JmDataSet) getTableViewer().getElementAt(index);
			context.deleteDataSet(subject.toReference());
			
			tableViewer.remove(subject);
			int nextSelection = table.getItemCount() > index ? index : index - 1;
			if (nextSelection >= 0) {
				table.setSelection(nextSelection);
				enableEditorControls(nextSelection);
			} else {
				disableEditorControls();
			}
			table.setFocus();
		}
		
		private void updateModel() {
			int editIndex = getTableViewer().getTable().getSelectionIndex();
			if (editIndex == -1) {
				return;
			}
			
			SimpleJmDataSet dataSetModel = (SimpleJmDataSet) context.getDataSets().get(editIndex);
			dataSetModel.setName(StringUtils.defaultString(txtDataSetName.getText()));
		}
		

		private class EditListenerImpl extends AbstractEditListener {
			
			@Override
			protected void process(TypedEvent e) {
				updateModel();
			}
		}
	}
}
