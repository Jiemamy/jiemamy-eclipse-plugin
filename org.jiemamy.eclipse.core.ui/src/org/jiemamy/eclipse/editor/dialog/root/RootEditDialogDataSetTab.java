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
package org.jiemamy.eclipse.editor.dialog.root;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
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
import org.jiemamy.JiemamyEntity;
import org.jiemamy.eclipse.editor.dialog.AbstractEditListener;
import org.jiemamy.eclipse.editor.dialog.EditListener;
import org.jiemamy.eclipse.ui.AbstractTableEditor;
import org.jiemamy.eclipse.ui.DefaultTableEditorConfig;
import org.jiemamy.eclipse.ui.helper.TextSelectionAdapter;
import org.jiemamy.eclipse.ui.tab.AbstractTab;
import org.jiemamy.model.dataset.DataSetModel;
import org.jiemamy.transaction.Command;
import org.jiemamy.transaction.CommandListener;
import org.jiemamy.transaction.EventBroker;
import org.jiemamy.transaction.SavePoint;
import org.jiemamy.utils.LogMarker;

/**
 * データベース編集ダイアログの「データセット」タブ。
 * 
 * @author daisuke
 */
public class RootEditDialogDataSetTab extends AbstractTab {
	
	private static Logger logger = LoggerFactory.getLogger(RootEditDialogDataSetTab.class);
	
	private final JiemamyContext rootModel;
	
	private AbstractTableEditor dataSetTableEditor;
	
	/** モデル操作に用いるファサード */
	private final JiemamyFacade jiemamyFacade;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param parentTabFolder 親となるタブフォルダ
	 * @param style SWTスタイル値
	 * @param rootModel 編集対象{@link JiemamyContext}
	 * @param jiemamyFacade モデル操作に用いるファサード
	 */
	public RootEditDialogDataSetTab(TabFolder parentTabFolder, int style, JiemamyContext rootModel,
			JiemamyFacade jiemamyFacade) {
		super(parentTabFolder, style, "データセット(&T)"); // RESOURCE
		
		this.rootModel = rootModel;
		this.jiemamyFacade = jiemamyFacade;
		
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
	private class DataSetContentProvider extends ArrayContentProvider implements CommandListener {
		
		private Viewer viewer;
		

		public void commandExecuted(Command command) {
			logger.debug(LogMarker.LIFECYCLE, "DataSetContentProvider: commandExecuted");
			dataSetTableEditor.refreshTable(); // レコードの変更を反映させる。
		}
		
		public void commandExecuted(Command command) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void dispose() {
			logger.debug(LogMarker.LIFECYCLE, "DataSetContentProvider: disposed");
			super.dispose();
		}
		
		public JiemamyEntity getTargetModel() {
			return (JiemamyEntity) viewer.getInput();
		}
		
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			logger.debug(LogMarker.LIFECYCLE, "DataSetContentProvider: input changed");
			logger.trace(LogMarker.LIFECYCLE, "oldInput: " + oldInput);
			logger.trace(LogMarker.LIFECYCLE, "newInput: " + newInput);
			
			this.viewer = viewer;
			
			super.inputChanged(viewer, oldInput, newInput);
		}
		
	}
	
	/**
	 * DataSet用LabelProvider実装クラス。
	 * 
	 * @author daisuke
	 */
	private class DataSetLabelProvider extends BaseLabelProvider implements ITableLabelProvider {
		
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		
		public String getColumnText(Object element, int columnIndex) {
			DataSetModel dataSet = (DataSetModel) element;
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
		
		private final JiemamyContext jiemamy;
		
		private Text txtDataSetName;
		
		private Button btnEdit;
		
		private final List<DataSetModel> dataSets;
		

		/**
		 * インスタンスを生成する。
		 * 
		 * @param parent 親コンポーネント
		 * @param style SWTスタイル値
		 */
		public DataSetTableEditor(Composite parent, int style) {
			super(parent, style, new DefaultTableEditorConfig("データセット情報")); // RESOURCE
			
			jiemamy = rootModel.getJiemamy();
			dataSets = rootModel.getDataSets();
			
			assert jiemamy != null;
			assert dataSets != null;
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
					DataSetModel dataSetModel = rootModel.getDataSets().get(selectionIndex);
					DataSetEditDialog dataSetEditDialog =
							new DataSetEditDialog(getShell(), dataSetModel, jiemamyFacade);
					SavePoint save = jiemamyFacade.save();
					if (dataSetEditDialog.open() != Window.OK) {
						jiemamyFacade.rollback(save);
					}
				}
			});
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
			tableViewer.setLabelProvider(new DataSetLabelProvider());
			final DataSetContentProvider contentProvider = new DataSetContentProvider();
			tableViewer.setContentProvider(contentProvider);
			tableViewer.setInput(dataSets);
			
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
			DataSetModel dataSet = dataSets.get(index);
			
			txtDataSetName.setEnabled(true);
			btnEdit.setEnabled(true);
			
			// 現在値の設定
			txtDataSetName.setText(JiemamyPropertyUtil.careNull(dataSet.getName()));
		}
		
		@Override
		protected JiemamyEntity performAddItem() {
			Table table = getTableViewer().getTable();
			JiemamyFactory factory = jiemamy.getFactory();
			DataSetModel dataSetModel = factory.newModel(DataSetModel.class);
			
			String newName = "DATASET_" + (dataSets.size() + 1);
			jiemamyFacade.changeModelProperty(dataSetModel, DataSetProperty.name, newName);
			
			jiemamyFacade.addDataSet(dataSetModel);
			
			int addedIndex = dataSets.indexOf(dataSetModel);
			table.setSelection(addedIndex);
			enableEditControls(addedIndex);
			txtDataSetName.setFocus();
			
			return dataSetModel;
		}
		
		@Override
		protected JiemamyEntity performInsertItem() {
			Table table = getTableViewer().getTable();
			int index = table.getSelectionIndex();
			
			JiemamyFactory factory = jiemamy.getFactory();
			DataSetModel dataSetModel = factory.newModel(DataSetModel.class);
			
			if (index < 0 || index > table.getItemCount()) {
				jiemamyFacade.addDataSet(dataSetModel);
			} else {
				jiemamyFacade.addDataSet(dataSetModel, index);
			}
			
			int addedIndex = dataSets.indexOf(dataSetModel);
			table.setSelection(addedIndex);
			enableEditControls(addedIndex);
			txtDataSetName.setFocus();
			
			return dataSetModel;
		}
		
		@Override
		protected void performMoveDownItem() {
			Table table = getTableViewer().getTable();
			int index = table.getSelectionIndex();
			if (index < 0 || index >= table.getItemCount()) {
				return;
			}
			
			jiemamyFacade.swapListElement(rootModel, dataSets, index, index + 1);
			
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
			
			jiemamyFacade.swapListElement(rootModel, dataSets, index, index - 1);
			
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
			
			DataSetModel dataSetModel = dataSets.get(index);
			jiemamyFacade.removeDataSet(dataSetModel);
			tableViewer.remove(dataSetModel);
			int nextSelection = table.getItemCount() > index ? index : index - 1;
			if (nextSelection >= 0) {
				table.setSelection(nextSelection);
				enableEditorControls(nextSelection);
			} else {
				disableEditorControls();
			}
			table.setFocus();
			
			return dataSetModel;
		}
		
		private void updateModel() {
			int editIndex = getTableViewer().getTable().getSelectionIndex();
			
			if (editIndex == -1) {
				return;
			}
			DataSetModel dataSetModel = dataSets.get(editIndex);
			dataSetModel.setName(JiemamyPropertyUtil.careNull(txtDataSetName.getText(), true));
		}
		

		private class EditListenerImpl extends AbstractEditListener {
			
			@Override
			protected void process(TypedEvent e) {
				updateModel();
				dataSetTableEditor.refreshTable();
			}
		}
	}
}
