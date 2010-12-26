/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
 * Created on 2008/08/03
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
package org.jiemamy.eclipse.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.CommonMessages;
import org.jiemamy.eclipse.ui.FileSelectWizardPage;
import org.jiemamy.eclipse.utils.ExceptionHandler;

/**
 * ダイアグラム画像保存機能アクション。
 * 
 * @author daisuke
 */
public class SaveDiagramImageAction extends AbstractJiemamyAction {
	
	private static final int MARGIN = 50;
	
	private File file;
	
	private int format;
	
	private boolean overwrite;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param viewer ビューア
	 */
	public SaveDiagramImageAction(GraphicalViewer viewer) {
		super(Messages.SaveDiagramImageAction_name, viewer);
	}
	
	@Override
	public void run() {
		if (prepare()) {
			try {
				export(getViewer(), file, format, overwrite);
			} catch (IOException e) {
				ExceptionHandler.handleException(e);
			}
		}
	}
	
	private void export(GraphicalViewer viewer, File file, int format, boolean overwrite) throws IOException {
		IFigure figure = ((AbstractGraphicalEditPart) viewer.getRootEditPart()).getFigure();
		
		if (file.exists() && overwrite == false) {
			if (MessageDialog.openQuestion(null, Messages.GraphicWizard_title,
					NLS.bind(CommonMessages.Common_fileOverwrite, file.getPath())) == false) {
				return;
			}
		} else if (file.createNewFile() == false) {
			throw new IOException();
		}
		
		if (figure instanceof Viewport) {
			// Reinit the figure
			Viewport viewport = (Viewport) figure;
			viewport.setViewLocation(0, 0);
		}
		
		Dimension size = figure.getPreferredSize();
		Image image = new Image(Display.getDefault(), size.width + MARGIN, size.height + MARGIN);
		GC gc = new GC(image);
		SWTGraphics graphics = new SWTGraphics(gc);
		figure.paint(graphics);
		
		ImageLoader loader = new ImageLoader();
		loader.data = new ImageData[] {
			image.getImageData()
		};
		
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			loader.save(out, format);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}
	
	private boolean prepare() {
		JiemamyContext rootModel = (JiemamyContext) getViewer().getContents().getModel();
		
		WizardDialog dialog = new WizardDialog(null, new GraphicWizard(rootModel));
		
		if (dialog.open() == Dialog.CANCEL) {
			return false;
		}
		
		overwrite = false;
		if (file.exists()
				&& MessageDialog.openConfirm(null, Messages.GraphicWizard_title,
						NLS.bind(CommonMessages.Common_fileOverwrite, file.getPath()))) {
			overwrite = true;
		}
		return true;
	}
	

	private class GraphicWizard extends Wizard {
		
		private GraphicWizardPage page;
		

		/**
		 * インスタンスを生成する。
		 * 
		 * @param rootModel ルートモデル
		 */
		public GraphicWizard(JiemamyContext rootModel) {
			setWindowTitle(Messages.GraphicWizard_title);
		}
		
		@Override
		public void addPages() {
			page = new GraphicWizardPage();
			addPage(page);
		}
		
		@Override
		public boolean performFinish() {
			file = new File(page.getPath());
			format = page.getFormat();
			return true;
		}
	}
	
	private static class GraphicWizardPage extends FileSelectWizardPage {
		
		private Combo cmbFormat;
		
		private List<ImageFileFormat> formatList = new ArrayList<ImageFileFormat>(6);
		

		/**
		 * インスタンスを生成する。
		 */
		public GraphicWizardPage() {
			super("GraphicWizardPage", Messages.GraphicWizard_title, null, new String[] {
				"すべて"
			}, new String[] {
				"*.*"
			});
			setDescription(Messages.GraphicWizard_description);
			
			formatList.add(new ImageFileFormat(Messages.FileFormat_jpg_description, "jpeg", SWT.IMAGE_JPEG));
			// THINK GIF は org.eclipse.swt.SWTException: Unsupported color depth が飛ぶ。何故？
			formatList.add(new ImageFileFormat("GIF file format", "gif", SWT.IMAGE_GIF));
			// THINK PNG は org.eclipse.swt.SWTException: Unsupported or unrecognized format が飛ぶ。何故？
			formatList.add(new ImageFileFormat("PNG file format", "png", SWT.IMAGE_PNG));
			formatList.add(new ImageFileFormat(Messages.FileFormat_bmp_description, "bmp", SWT.IMAGE_BMP));
			formatList.add(new ImageFileFormat(Messages.FileFormat_bmpRLE_description, "bmp", SWT.IMAGE_BMP_RLE));
			formatList.add(new ImageFileFormat(Messages.FileFormat_ico_description, "ico", SWT.IMAGE_ICO));
		}
		
		@Override
		public void createControl(Composite parent) {
			super.createControl(parent);
			
			Composite composite = (Composite) getControl();
			
			Label label = new Label(composite, SWT.NULL);
			label.setText(Messages.GraphicWizard_fileFormat_label);
			
			cmbFormat = new Combo(composite, SWT.READ_ONLY);
			for (ImageFileFormat format : formatList) {
				cmbFormat.add(format.getFormatDescription());
			}
			
			cmbFormat.setText(formatList.get(0).getFormatDescription());
			cmbFormat.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					String path = getPath();
					int extensionIndex = path.lastIndexOf(".") + 1;
					
					StringBuilder sb = new StringBuilder();
					if (extensionIndex > 0) {
						sb.append(path.substring(0, extensionIndex));
						
					} else {
						sb.append(path).append(".");
					}
					sb.append(getExtension());
					setPath(sb.toString());
				}
			});
			cmbFormat.setFocus();
		}
		
		/**
		 * 拡張子を取得する。
		 * 
		 * @return 拡張子
		 */
		public String getExtension() {
			return formatList.get(cmbFormat.getSelectionIndex()).getExtension();
		}
		
		/**
		 * 出力フォーマットを取得する。
		 * @return 出力フォーマット
		 */
		public int getFormat() {
			return formatList.get(cmbFormat.getSelectionIndex()).getFormat();
		}
		

		private static class ImageFileFormat {
			
			/** ファイルフォーマットの説明文 */
			private String formatDescription;
			
			/** 拡張子 */
			private String extension;
			
			/** ファイルフォーマット */
			private int format;
			

			/**
			 * インスタンスを生成する。
			 * 
			 * @param formatDescription ファイルフォーマットの説明文
			 * @param extension 拡張子
			 * @param format ファイルフォーマット
			 */
			public ImageFileFormat(String formatDescription, String extension, int format) {
				this.formatDescription = formatDescription;
				this.format = format;
				this.extension = extension;
			}
			
			/**
			 * 拡張子を取得する。
			 * @return 拡張子
			 */
			public String getExtension() {
				return extension;
			}
			
			/**
			 * ファイルフォーマットを取得する。
			 * @return ファイルフォーマット
			 */
			public int getFormat() {
				return format;
			}
			
			/**
			 * ファイルフォーマットの説明文を取得する。
			 * @return ファイルフォーマットの説明文
			 */
			public String getFormatDescription() {
				return formatDescription;
			}
		}
	}
}
