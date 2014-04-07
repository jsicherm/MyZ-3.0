/**
 * 
 */
package myz.nmscode.v1_7_R2.mobs;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import myz.support.interfacing.Messenger;
import net.minecraft.server.v1_7_R2.BiomeBase;
import net.minecraft.server.v1_7_R2.BiomeMeta;
import net.minecraft.server.v1_7_R2.EntityGiantZombie;
import net.minecraft.server.v1_7_R2.EntityHorse;
import net.minecraft.server.v1_7_R2.EntityInsentient;
import net.minecraft.server.v1_7_R2.EntityPigZombie;
import net.minecraft.server.v1_7_R2.EntitySkeleton;
import net.minecraft.server.v1_7_R2.EntityTypes;
import net.minecraft.server.v1_7_R2.EntityZombie;

import org.bukkit.entity.EntityType;

/**
 * @author Jordan
 * 
 */
public enum CustomEntityType {

	ZOMBIE("Zombie", 54, EntityType.ZOMBIE, EntityZombie.class, CustomEntityZombie.class), GIANT("Giant", 53, EntityType.GIANT,
			EntityGiantZombie.class, CustomEntityGiantZombie.class), PIGMAN("PigZombie", 57, EntityType.PIG_ZOMBIE, EntityPigZombie.class,
			CustomEntityPigZombie.class), HORSE("Horse", 100, EntityType.HORSE, EntityHorse.class, CustomEntityHorse.class), NPC(
			"Skeleton", 51, EntityType.SKELETON, EntitySkeleton.class, CustomEntityNPC.class);

	private String name;
	private int id;
	private EntityType entityType;
	private Class<? extends EntityInsentient> nmsClass;
	private Class<? extends EntityInsentient> customClass;

	private CustomEntityType(String name, int id, EntityType entityType, Class<? extends EntityInsentient> nmsClass,
			Class<? extends EntityInsentient> customClass) {
		this.name = name;
		this.id = id;
		this.entityType = entityType;
		this.nmsClass = nmsClass;
		this.customClass = customClass;
	}

	private static void a(Class<?> paramClass, String paramString, int paramInt) {
		try {
			((Map) getPrivateStatic(EntityTypes.class, "c")).put(paramString, paramClass);
			((Map) getPrivateStatic(EntityTypes.class, "d")).put(paramClass, paramString);
			((Map) getPrivateStatic(EntityTypes.class, "e")).put(Integer.valueOf(paramInt), paramClass);
			((Map) getPrivateStatic(EntityTypes.class, "f")).put(paramClass, Integer.valueOf(paramInt));
			((Map) getPrivateStatic(EntityTypes.class, "g")).put(paramString, Integer.valueOf(paramInt));
		} catch (Exception exc) {
			Messenger.sendConsoleMessage("&4Registration issue!");
		}
	}

	private static Object getPrivateStatic(Class<?> clazz, String f) throws NoSuchFieldException, SecurityException, IllegalArgumentException,
			IllegalAccessException {
		Field field = clazz.getDeclaredField(f);
		field.setAccessible(true);
		return field.get(null);
	}

	public static void registerEntities() {
		for (CustomEntityType entity : values())
			a(entity.getCustomClass(), entity.getName(), entity.getID());

		BiomeBase[] biomes;
		try {
			biomes = (BiomeBase[]) getPrivateStatic(BiomeBase.class, "biomes");
		} catch (Exception exc) {
			Messenger.sendConsoleMessage("&4BiomeBase issue!");
			return;
		}
		for (BiomeBase biomeBase : biomes) {
			if (biomeBase == null)
				break;

			for (String field : new String[] { "as", "at", "au", "av" })
				try {
					Field list = BiomeBase.class.getDeclaredField(field);
					list.setAccessible(true);
					@SuppressWarnings("unchecked")
					List<BiomeMeta> mobList = (List<BiomeMeta>) list.get(biomeBase);

					for (BiomeMeta meta : mobList)
						for (CustomEntityType entity : values())
							if (entity.getNMSClass().equals(meta.b))
								meta.b = entity.getCustomClass();
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}

	public static void unregisterEntities() {
		for (CustomEntityType entity : values()) {
			try {
				((Map) getPrivateStatic(EntityTypes.class, "d")).remove(entity.getCustomClass());
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				((Map) getPrivateStatic(EntityTypes.class, "f")).remove(entity.getCustomClass());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for (CustomEntityType entity : values())
			try {
				a(entity.getNMSClass(), entity.getName(), entity.getID());
			} catch (Exception e) {
				e.printStackTrace();
			}

		BiomeBase[] biomes;
		try {
			biomes = (BiomeBase[]) getPrivateStatic(BiomeBase.class, "biomes");
		} catch (Exception exc) {
			Messenger.sendConsoleMessage("&4BiomeBase issue!");
			return;
		}
		for (BiomeBase biomeBase : biomes) {
			if (biomeBase == null)
				break;

			for (String field : new String[] { "as", "at", "au", "av" })
				try {
					Field list = BiomeBase.class.getDeclaredField(field);
					list.setAccessible(true);
					@SuppressWarnings("unchecked")
					List<BiomeMeta> mobList = (List<BiomeMeta>) list.get(biomeBase);

					for (BiomeMeta meta : mobList)
						for (CustomEntityType entity : values())
							if (entity.getCustomClass().equals(meta.b))
								meta.b = entity.getNMSClass();
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}

	public Class<? extends EntityInsentient> getCustomClass() {
		return customClass;
	}

	public EntityType getEntityType() {
		return entityType;
	}

	public int getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Class<? extends EntityInsentient> getNMSClass() {
		return nmsClass;
	}
}