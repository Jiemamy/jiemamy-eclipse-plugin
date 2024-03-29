/*
 * Copyright 2007-2012 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.core.ui.editor;

import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteSeparator;
import org.eclipse.gef.palette.PanningSelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.jface.resource.ImageRegistry;

import org.jiemamy.eclipse.core.ui.Images;
import org.jiemamy.eclipse.core.ui.JiemamyUIPlugin;
import org.jiemamy.eclipse.core.ui.editor.diagram.connection.ForeignKeyCreation;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.sticky.StickyCreation;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.table.TableCreation;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.view.ViewCreation;
import org.jiemamy.model.JmStickyNode;

/**
 * {@link JiemamyDiagramEditor}用のパレット（デフォルトで右側にある奴）を生成するファクトリ。
 * 
 * @author daisuke
 */
public final class DiagramEditorPaletteFactory {
	
	private static ImageRegistry imageRegistry = JiemamyUIPlugin.getDefault().getImageRegistry();
	
	
	/**
	 * エディタパレットを生成する。
	 * 
	 * @return 生成したエディタパレット
	 */
	public static PaletteRoot createPalette() {
		PaletteRoot palette = new PaletteRoot();
		
		// add basic tools to palette (selection tool and marquee tool)
		palette.add(createBasicToolsGroup(palette));
		
		// add entity tools to palette
		palette.add(createEntityDrawer(palette));
		
		// add relation tools to palette
		palette.add(createRelationDrawer(palette));
		
		// add other tools to palette
		palette.add(createOtherDrawer(palette));
		
		return palette;
	}
	
//	/**
//	 * FlyoutPaletteの設定を生成する。
//	 * @return FlyoutPaletteの設定
//	 */
//	protected static FlyoutPreferences createPalettePreferences() {
//		return new FlyoutPreferences() {
//			
//			public int getDockLocation() {
//				return getPreferenceStore().getInt(PreferenceKey.PALETTE_DOCK_LOCATION.toString());
//			}
//			
//			public int getPaletteState() {
//				return getPreferenceStore().getInt(PreferenceKey.PALETTE_STATE.toString());
//			}
//			
//			public int getPaletteWidth() {
//				return getPreferenceStore().getInt(PreferenceKey.PALETTE_SIZE.toString());
//			}
//			
//			public void setDockLocation(int location) {
//				getPreferenceStore().setValue(PreferenceKey.PALETTE_DOCK_LOCATION.toString(), location);
//			}
//			
//			public void setPaletteState(int state) {
//				getPreferenceStore().setValue(PreferenceKey.PALETTE_STATE.toString(), state);
//			}
//			
//			public void setPaletteWidth(int width) {
//				getPreferenceStore().setValue(PreferenceKey.PALETTE_SIZE.toString(), width);
//			}
//			
//			private IPreferenceStore getPreferenceStore() {
//				return JiemamyPlugin.getDefault().getPreferenceStore();
//			}
//		};
//	}
	
	private static PaletteContainer createBasicToolsGroup(PaletteRoot palette) {
		PaletteGroup toolGroup = new PaletteGroup("palette.tools"); // RESOURCE
		
		// Add a selection tool to the group
		ToolEntry tool = new PanningSelectionToolEntry(); // THINK new SelectionToolEntry() とどう違う？
		toolGroup.add(tool);
		palette.setDefaultEntry(tool);
		
		// Add a marquee tool to the group
		toolGroup.add(new MarqueeToolEntry());
		
		// Add a (unnamed) separator to the group
		toolGroup.add(new PaletteSeparator());
		
		return toolGroup;
	}
	
	private static PaletteContainer createEntityDrawer(PaletteRoot palette) {
		PaletteDrawer drawer = new PaletteDrawer("エンティティ"); // RESOURCE
		
		CombinedTemplateCreationEntry tableCreationEntry = new CombinedTemplateCreationEntry("テーブル", // RESOURCE
				"新しいテーブルを作成します", // RESOURCE
				new CreationFactory() {
					
					public Object getNewObject() {
						return new TableCreation();
					}
					
					public Object getObjectType() {
						return TableCreation.class;
					}
					
				}, imageRegistry.getDescriptor(Images.BUTTON_TABLE), imageRegistry.getDescriptor(Images.BUTTON_TABLE));
		drawer.add(tableCreationEntry);
		
		CombinedTemplateCreationEntry viewCreationEntry = new CombinedTemplateCreationEntry("ビュー", // RESOURCE
				"新しいビューを作成します。", // RESOURCE
				new CreationFactory() {
					
					public Object getNewObject() {
						return new ViewCreation();
					}
					
					public Object getObjectType() {
						return ViewCreation.class;
					}
					
				}, imageRegistry.getDescriptor(Images.BUTTON_VIEW), imageRegistry.getDescriptor(Images.BUTTON_VIEW));
		drawer.add(viewCreationEntry);
		
		return drawer;
	}
	
	private static PaletteContainer createOtherDrawer(PaletteRoot palette) {
		PaletteDrawer drawer = new PaletteDrawer("その他"); // RESOURCE
		
		CombinedTemplateCreationEntry stickyCreationEntry = new CombinedTemplateCreationEntry("メモ", // RESOURCE
				"新しいメモを作成します。", // RESOURCE
				new CreationFactory() {
					
					public Object getNewObject() {
						return new StickyCreation();
					}
					
					public Object getObjectType() {
						return JmStickyNode.class;
					}
					
				}, imageRegistry.getDescriptor(Images.BUTTON_VIEW), imageRegistry.getDescriptor(Images.BUTTON_VIEW));
		drawer.add(stickyCreationEntry);
		
		return drawer;
	}
	
	private static PaletteContainer createRelationDrawer(PaletteRoot palette) {
		PaletteDrawer drawer = new PaletteDrawer("コネクション"); // RESOURCE
		
		ConnectionCreationToolEntry foreignKeyCreationEntry = new ConnectionCreationToolEntry("外部キー", // RESOURCE
				"新しい外部キーを定義します。", // RESOURCE
				new CreationFactory() {
					
					public Object getNewObject() {
						return new ForeignKeyCreation();
					}
					
					public Object getObjectType() {
						return ForeignKeyCreation.class;
					}
					
				}, imageRegistry.getDescriptor(Images.BUTTON_FK), imageRegistry.getDescriptor(Images.BUTTON_FK));
		drawer.add(foreignKeyCreationEntry);
		
		return drawer;
	}
	
	/**
	 * ユーティリティクラス。
	 */
	private DiagramEditorPaletteFactory() {
	}
	
}
