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
package org.evilco.defense.common.tile.generic;

import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import org.evilco.defense.common.entity.SecurityBotEntity;
import org.evilco.defense.common.tile.network.*;
import org.evilco.defense.util.Location;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

/**
 * @author 		Johannes Donath <johannesd@evil-co.com>
 * @copyright		Copyright (C) 2014 Evil-Co <http://www.evil-co.org>
 */
public class DefenseStationTileEntity extends TileEntity implements ISurveillanceNetworkHub {

	/**
	 * Indicates whether the entity is active.
	 */
	protected boolean isActive = true;

	/**
	 * Stores a list of user identifiers which may pass through protected zones.
	 */
	protected Map<UUID, String> knownUsers = new HashMap<UUID, String> ();

	/**
	 * Stores the owner's UUID.
	 */
	protected UUID owner = null;

	/**
	 * Stores all currently connected clients.
	 */
	protected List<ISurveillanceNetworkClient> connectedClients = new ArrayList<ISurveillanceNetworkClient> ();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connectEntity (ISurveillanceNetworkClient entity) throws SurveillanceEntityConnectionException {
		if (!this.connectedClients.contains (entity)) this.connectedClients.add (entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void disconnectEntity (ISurveillanceNetworkClient entity) {
		this.connectedClients.remove (entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Packet getDescriptionPacket () {
		NBTTagCompound nbtTagCompound = new NBTTagCompound ();
		this.writeToNBT (nbtTagCompound);

		return (new S35PacketUpdateTileEntity (this.xCoord, this.yCoord, this.zCoord, 1, nbtTagCompound));
	}

	/**
	 * Returns a list of known users.
	 * @return The list.
	 */
	public Map<UUID, String> getKnownUsers () {
		return this.knownUsers;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Location getLocation () {
		return (new Location (this.xCoord, this.yCoord, this.zCoord));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UUID getOwner () {
		return this.owner;
	}

	/**
	 * Returns the camera rotation angle (in 90 degree steps).
	 * @return The rotation.
	 */
	public float getRotationAngle () {
		return (this.getBlockMetadata () * 90.0f);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isActive () {
		return this.isActive;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receiveMessage (ISurveillanceNetworkPacket packet) {
		// parse camera packets
		if (packet instanceof CameraDetectionPacket) {
			// cast packet
			CameraDetectionPacket detectionPacket = ((CameraDetectionPacket) packet);

			// iterate over entities
			int knownEntities = 0;
			EntityPlayer player = null;

			for (Entity entity : detectionPacket.getDetectedEntities ()) {
				// verify players
				if (entity instanceof EntityPlayer) {
					// cast player
					player = ((EntityPlayer) entity);

					// verify against known users
					if (this.knownUsers.containsKey (player.getPersistentID ())) knownEntities++;
				}
			}

			// create alert packet
			if (detectionPacket.getDetectedEntities ().size () > 0 && (detectionPacket.getDetectedEntities ().size () > knownEntities)) {
				Entity entity = detectionPacket.getDetectedEntities ().get (0);

				DefenseOrderPacket defenseOrderPacket = new DefenseOrderPacket (this, new Location (entity.posX, entity.posY, entity.posZ));

				// notify all connected entities
				for (ISurveillanceNetworkClient client : this.connectedClients) {
					client.receiveMessage (defenseOrderPacket);
				}
			}
			return;
		}

		// parse attack orders
		if (packet instanceof  AttackOrderRequestPacket) {
			// cast packet
			AttackOrderRequestPacket attackOrderPacket = ((AttackOrderRequestPacket) packet);

			// iterate over all possible targets
			for (EntityLivingBase entityLiving : attackOrderPacket.getPossibleTargets ()) {
				// check for security bot
				if (entityLiving instanceof SecurityBotEntity) continue;

				// check for player
				if (entityLiving instanceof EntityPlayer) {
					// cast to player
					EntityPlayer entityPlayer = ((EntityPlayer) entityLiving);

					// verify UUID against this known users
					if (entityPlayer.capabilities.isCreativeMode) continue;
					if (this.knownUsers.containsKey (entityPlayer.getPersistentID ())) continue;
				}

				// skip passive mods
				if (entityLiving instanceof EntityAnimal) continue;

				// send attack order
				AttackOrderPacket attackPacket = new AttackOrderPacket (this, entityLiving);
				attackOrderPacket.getSource ().receiveMessage (attackPacket);

				// only attack one at a time
				break;
			}

			return;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onChunkUnload () {
		super.onChunkUnload ();

		// disable hub
		this.isActive = false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDataPacket (NetworkManager net, S35PacketUpdateTileEntity pkt) {
		this.readFromNBT (pkt.func_148857_g ());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void readFromNBT (NBTTagCompound p_145839_1_) {
		super.readFromNBT (p_145839_1_);

		// read owner
		if (p_145839_1_.hasKey ("owner"))
			this.owner = UUID.fromString (p_145839_1_.getString ("owner"));
		else
			this.owner = null;

		// get a string list of known users
		if (p_145839_1_.hasKey ("knownUsers")) {
			NBTTagList tagList = p_145839_1_.getTagList ("knownUsers", Constants.NBT.TAG_COMPOUND);

			// create empty list
			this.knownUsers = new HashMap<UUID, String> ();

			// iterate over list items
			for (int i = 0; i < tagList.tagCount (); i++) {
				// get tag
				NBTTagCompound compound = tagList.getCompoundTagAt (i);

				// get elements
				this.knownUsers.put (UUID.fromString (compound.getString ("uuid")), compound.getString ("username"));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setOwner (UUID owner) {
		throw new NotImplementedException ();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setOwner (UUID owner, String username) {
		// remove old owner
		if (this.owner != null) this.knownUsers.remove (owner);

		// set new owner
		this.owner = owner;
		this.knownUsers.put (owner, username);

		// update tile entity
		this.worldObj.markTileEntityChunkModified (this.xCoord, this.yCoord, this.zCoord, this);
		this.worldObj.markBlockForUpdate (this.xCoord, this.yCoord, this.zCoord);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setOwner (EntityPlayer player) {
		this.setOwner (player.getPersistentID (), player.getDisplayName ());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeToNBT (NBTTagCompound p_145841_1_) {
		super.writeToNBT (p_145841_1_);

		// set owner
		if (this.owner != null) p_145841_1_.setString ("owner", this.owner.toString ());

		// write list of known users
		if (this.knownUsers != null) {
			// create new tag
			NBTTagList tagList = new NBTTagList ();

			// append all known users
			for (Map.Entry<UUID, String> user : this.knownUsers.entrySet ()) {
				// create new tag compound
				NBTTagCompound compound = new NBTTagCompound ();

				// set data
				compound.setString ("uuid", user.getKey ().toString ());
				compound.setString ("username", user.getValue ().toString ());

				// append tag
				tagList.appendTag (compound);
			}

			// append tag
			p_145841_1_.setTag ("knownUsers", tagList);
		}
	}
}