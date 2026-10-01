-- 新增「ppt-master」前台菜单：挂在「数字人平台」(parent_id=2763) 下，
-- 与「PPT制作」(id=2836) 平级，组件指向 digital/ppt-master。
-- 权限随 PPT制作：分配给同样的角色（超级管理员 + 演示/业务等）。
INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
    (2837, 'ppt-master', '', 2, 8, 2763, 'ppt-master', 'ep:document', 'digital/ppt-master', 'ppt-master', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

INSERT INTO `system_role_menu`
    (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
    (6938, 1,   2837, '1', NOW(), '1', NOW(), b'0', 0),
    (6939, 112, 2837, '1', NOW(), '1', NOW(), b'0', 0),
    (6940, 115, 2837, '1', NOW(), '1', NOW(), b'0', 0),
    (6941, 116, 2837, '1', NOW(), '1', NOW(), b'0', 0),
    (6942, 117, 2837, '1', NOW(), '1', NOW(), b'0', 0),
    (6943, 118, 2837, '1', NOW(), '1', NOW(), b'0', 0),
    (6944, 119, 2837, '1', NOW(), '1', NOW(), b'0', 0),
    (6945, 120, 2837, '1', NOW(), '1', NOW(), b'0', 0);
