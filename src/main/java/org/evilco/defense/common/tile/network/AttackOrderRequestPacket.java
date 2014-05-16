/*
 * Copyright (C) 2014 Evil-Co <http://wwww.evil-co.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.evilco.defense.common.tile.network;

import net.minecraft.entity.EntityLivingBase;

import java.util.List;

/**
 * @author 		Johannes Donath <johannesd@evil-co.com>
 * @copyright		Copyright (C) 2014 Evil-Co <http://www.evil-co.org>
 */
public class AttackOrderRequestPacket implements ISurveillanceNetworkPacket {

	/**
	 * Stores the packet source.
	 */
	protected ISurveillanceNetworkEntity source = null;

	/**
	 * Stores an ordered list of possible targets.
	 */
	protected List<EntityLivingBase> possibleTargets = null;

	/**
	 * Constructs a new AttackOrderRequestPacket.
	 * @param source
	 * @param possibleTargets
	 */
	public AttackOrderRequestPacket (ISurveillanceNetworkEntity source, List<EntityLivingBase> possibleTargets) {
		this.source = source;
		this.possibleTargets = possibleTargets;
	}

	/**
	 * Returns a list of possible targets to choose from.
	 * @return The entity list.
	 */
	public List<EntityLivingBase> getPossibleTargets () {
		return this.possibleTargets;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ISurveillanceNetworkEntity getSource () {
		return this.source;
	}
}