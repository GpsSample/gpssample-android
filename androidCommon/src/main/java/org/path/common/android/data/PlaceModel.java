/*
 * Copyright@ 2015 PATH
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under 
 * the License.
 *
 */

package org.path.common.android.data;

/*
 * @author belendia@gmail.com
 */
public class PlaceModel {
	private int id;

	public int getId() {
		return id;
	}

	public void setId(int value) {
		id = value;
	}

	private String hierarchyName;

	public String getHierarchyName() {
		return hierarchyName;
	}

	public void setHierarchyName(String value) {
		hierarchyName = value;
	}

	private String code;

	public String getCode() {
		return code;
	}

	public void setCode(String value) {
		code = value;
	}

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String value) {
		name = value;
	}

	private String relationship;

	public String getRelationship() {
		return relationship;
	}

	public void setRelationship(String value) {
		relationship = value;
	}
}
