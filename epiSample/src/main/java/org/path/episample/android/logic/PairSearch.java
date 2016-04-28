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

package org.path.episample.android.logic;

import java.util.ArrayList;

import org.path.common.android.data.CensusModel;
import org.path.episample.android.utilities.DistanceUtil;

/*
 * @author belendia@gmail.com
 */

public class PairSearch {

	public class Pair {
		private CensusModel point1 = null;
		private CensusModel point2 = null;
		private double distance = 0.0;

		public CensusModel getPoint1() {
			return point1;
		}

		public void setPoint1(CensusModel value) {
			point1 = value;
		}

		public CensusModel getPoint2() {
			return point2;
		}

		public void setPoint2(CensusModel value) {
			point2 = value;
		}

		public double getDistance() {
			return distance;
		}

		public void setDistance(double value) {
			distance = value;
		}
	}

	public ArrayList<Pair> getAllClosestPairs(ArrayList<CensusModel> points,
			long meters) {
		ArrayList<Pair> result = new ArrayList<Pair>();
		int totalNumberOfPoints = points.size();
		for (int i = 0; i < totalNumberOfPoints; i++) {
			for (int j = i + 1; j < totalNumberOfPoints; j++) {
				if (points.get(i).getInstanceId()
						.equals(points.get(j).getInstanceId()) == false) {
					double distance = DistanceUtil.getDistance(points.get(i)
							.getLatitude(), points.get(i).getLongitude(),
							points.get(j).getLatitude(), points.get(j)
									.getLongitude());
					if (distance <= meters) {
						Pair pair = new Pair();
						pair.setDistance(distance);
						pair.setPoint1(points.get(i));
						pair.setPoint2(points.get(j));
						result.add(pair);
					}
				}
			}
		}

		return result;
	}

	public ArrayList<Pair> getAllFarthestPairs(ArrayList<CensusModel> points,
			long meters) {
		ArrayList<Pair> result = new ArrayList<Pair>();
		int totalNumberOfPoints = points.size();
		for (int i = 0; i < totalNumberOfPoints; i++) {
			for (int j = i + 1; j < totalNumberOfPoints; j++) {
				if (points.get(i).getInstanceId()
						.equals(points.get(j).getInstanceId()) == false) {
					double distance = DistanceUtil.getDistance(points.get(i)
							.getLatitude(), points.get(i).getLongitude(),
							points.get(j).getLatitude(), points.get(j)
									.getLongitude());
					if (distance >= meters) {
						Pair pair = new Pair();
						pair.setDistance(distance);
						pair.setPoint1(points.get(i));
						pair.setPoint2(points.get(j));
						result.add(pair);
					}
				}
			}
		}

		return result;
	}
}
