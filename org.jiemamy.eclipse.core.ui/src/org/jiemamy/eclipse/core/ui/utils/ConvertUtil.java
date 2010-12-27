/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
 * Created on 2008/07/30
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
package org.jiemamy.eclipse.core.ui.utils;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import org.jiemamy.model.geometory.JmColor;
import org.jiemamy.model.geometory.JmPoint;
import org.jiemamy.model.geometory.JmRectangle;

/**
 * JiemamyモデルのインスタンスとSWT/Draw2Dのインスタンスを相互変換する。
 * 
 * @author daisuke
 */
public class ConvertUtil {
	
	/**
	 * {@link Color}を{@link JmColor}に変換する。
	 * 
	 * <p>{@code null}を与えた場合、{@code null}を返す。</p>
	 * 
	 * @param color 変換元
	 * @return {@link JmColor}のインスタンス
	 */
	public static Color convert(JmColor color) {
		return color == null ? null : new Color(null, color.red, color.green, color.blue);
	}
	
	/**
	 * {@link JmPoint}を{@link Point}に変換する。
	 * 
	 * <p>{@code null}を与えた場合、{@code null}を返す。</p>
	 * 
	 * @param point 変換元
	 * @return {@link Point}のインスタンス
	 */
	public static Point convert(JmPoint point) {
		return point == null ? null : new Point(point.x, point.y);
	}
	
	/**
	 * {@link JmRectangle}を{@link Rectangle}に変換する。
	 * 
	 * <p>{@code null}を与えた場合、{@code null}を返す。</p>
	 * 
	 * @param rectangle 変換元
	 * @return {@link Rectangle}のインスタンス
	 */
	public static Rectangle convert(JmRectangle rectangle) {
		return rectangle == null ? null : new Rectangle(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
	}
	
	/**
	 * {@link Point}を{@link JmPoint}に変換する。
	 * 
	 * <p>{@code null}を与えた場合、{@code null}を返す。</p>
	 * 
	 * @param point 変換元
	 * @return {@link JmPoint}のインスタンス
	 */
	public static JmPoint convert(Point point) {
		return point == null ? null : new JmPoint(point.x, point.y);
	}
	
	/**
	 * {@link Rectangle}を{@link JmRectangle}に変換する。
	 * 
	 * <p>{@code null}を与えた場合、{@code null}を返す。</p>
	 * 
	 * @param rectangle 変換元
	 * @return {@link JmRectangle}のインスタンス
	 */
	public static JmRectangle convert(Rectangle rectangle) {
		return rectangle == null ? null : new JmRectangle(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
	}
	
	/**
	 * {@link RGB}を{@link JmColor}に変換する。
	 * 
	 * <p>{@code null}を与えた場合、{@code null}を返す。</p>
	 * 
	 * @param rgb 変換元
	 * @return {@link JmColor}のインスタンス
	 */
	public static JmColor convert(RGB rgb) {
		return rgb == null ? null : new JmColor(rgb.red, rgb.green, rgb.blue);
	}
	
	private ConvertUtil() {
	}
}
