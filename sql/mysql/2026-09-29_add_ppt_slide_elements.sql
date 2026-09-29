-- PPT 细编辑共存（spec §8）：新增编辑态列，与语义态（ppt_slide_content）分离。
-- 执行前请备份；仅需在已部署环境执行一次。
ALTER TABLE `tb_ai_dh_copywrite_ppt_record_detail`
    ADD COLUMN `ppt_slide_elements` TEXT NULL COMMENT '每页 fabric 元素 JSON（编辑态，可空）' AFTER `ppt_slide_content`;
