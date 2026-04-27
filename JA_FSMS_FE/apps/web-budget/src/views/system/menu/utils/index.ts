import { SystemMenuApi } from '#/api';
/**
 * 在树形结构中根据菜单ID递归查找完整路径（父级+当前节点）
 * @param targetId 目标菜单ID
 * @param menus 树形菜单列表
 * @returns 完整路径数组，从根节点到目标节点
 */
export function getParentMenus(
  targetId: string,
  menus: SystemMenuApi.SystemMenu[],
): SystemMenuApi.SystemMenu[] {
  const fullPath: SystemMenuApi.SystemMenu[] = [];

  function findPathToTarget(
    id: string,
    menuList: SystemMenuApi.SystemMenu[],
    currentPath: SystemMenuApi.SystemMenu[],
  ): boolean {
    for (const menu of menuList) {
      const newPath = [...currentPath, menu];

      // 如果找到目标菜单
      if (menu.id === id) {
        // 将完整路径（包括目标菜单）加入数组
        fullPath.push(...newPath);
        return true;
      }

      // 如果有子菜单，递归查找
      if (menu.children && menu.children.length > 0) {
        const found = findPathToTarget(id, menu.children, newPath);
        if (found) {
          return true;
        }
      }
    }
    return false;
  }

  findPathToTarget(targetId, menus, []);

  // 如果没有找到路径，尝试直接查找目标菜单
  if (fullPath.length === 0) {
    function findMenuById(
      id: string,
      menuList: SystemMenuApi.SystemMenu[],
    ): null | SystemMenuApi.SystemMenu {
      for (const menu of menuList) {
        if (menu.id === id) {
          return menu;
        }
        if (menu.children && menu.children.length > 0) {
          const found = findMenuById(id, menu.children);
          if (found) return found;
        }
      }
      return null;
    }

    const currentMenu = findMenuById(targetId, menus);
    if (currentMenu) {
      fullPath.push(currentMenu);
    }
  }

  return fullPath;
}
