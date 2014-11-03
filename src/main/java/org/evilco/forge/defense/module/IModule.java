/*
 * Copyright 2014 Johannes Donath <johannesd@evil-co.com>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.evilco.forge.defense.module;

import net.minecraftforge.common.config.Configuration;

/**
 * @author Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.com>
 */
public interface IModule {

	/**
	 * Loads the module configuration.
	 * @param configuration The modification configuration.
	 */
	public void loadConfiguration (Configuration configuration);

	/**
	 * Registers module blocks.
	 */
	public void registerBlocks ();

	/**
	 * Registers modification block entities.
	 */
	public void registerBlockEntities ();

	/**
	 * Registers modification crafting recipes.
	 */
	public void registerCraftingRecipes ();

	/**
	 * Registers modification dimensions.
	 */
	public void registerDimensions (Configuration configuration);

	/**
	 * Registers modification entities.
	 */
	public void registerEntities ();

	/**
	 * Registers modification fluids.
	 */
	public void registerFluids ();

	/**
	 * Registers modification items.
	 */
	public void registerItems ();

	/**
	 * Registers modification potions.
	 */
	public void registerPotions ();

	/**
	 * Registers all modification renderers.
	 */
	public void registerRenderers ();
}
