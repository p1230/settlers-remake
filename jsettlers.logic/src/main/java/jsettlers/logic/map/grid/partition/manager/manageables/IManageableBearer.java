/*******************************************************************************
 * Copyright (c) 2015 - 2017
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.logic.map.grid.partition.manager.manageables;

import jsettlers.common.position.ILocatable;
import jsettlers.logic.map.grid.partition.manager.manageables.interfaces.IBarrack;
import jsettlers.logic.map.grid.partition.manager.materials.interfaces.IManagerBearer;
import jsettlers.logic.map.grid.partition.manager.materials.interfaces.IMaterialOffer;
import jsettlers.logic.map.grid.partition.manager.objects.WorkerCreationRequest;

/**
 * This interface defines methods needed by a bearer to be managed by a PartitionManager.
 *
 * @author Andreas Eberle
 */
public interface IManageableBearer extends IManageable, ILocatable, IManagerBearer {

	boolean becomeWorker(IWorkerRequester requester, WorkerCreationRequest request, IMaterialOffer offer);

	boolean becomeSoldier(IBarrack barrack);

	/**
	 * This interface is used by the bearers to signal the need of a reoffer of the worker creation request.
	 *
	 * @author Andreas Eberle
	 */
	interface IWorkerRequester {
		void workerCreationRequestFailed(WorkerCreationRequest failedRequest);

		void workerCreationRequestFulfilled(WorkerCreationRequest fulfilledRequest);
	}
}
