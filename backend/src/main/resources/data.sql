INSERT INTO bom_vehicle_model(model_code,model_name,platform,program,sop_date,status) SELECT 'M01','星耀 SUV','X平台','SOP','2025-06-01','ACTIVE' WHERE NOT EXISTS(SELECT 1 FROM bom_vehicle_model WHERE model_code='M01');
INSERT INTO bom_vehicle_model(model_code,model_name,platform,program,sop_date,status) SELECT 'M02','星耀轿车','Y平台','DESIGN','2026-01-01','ACTIVE' WHERE NOT EXISTS(SELECT 1 FROM bom_vehicle_model WHERE model_code='M02');
INSERT INTO bom_plant(plant_code,plant_name,sap_plant,address) SELECT 'P001','上海工厂','1000','上海市青浦区' WHERE NOT EXISTS(SELECT 1 FROM bom_plant WHERE plant_code='P001');
INSERT INTO bom_plant(plant_code,plant_name,sap_plant,address) SELECT 'P002','长沙工厂','2000','长沙市' WHERE NOT EXISTS(SELECT 1 FROM bom_plant WHERE plant_code='P002');
INSERT INTO bom_supplier(supplier_code,name,sap_vendor,srm_code,status) SELECT 'SUP01','华东汽车零部件','100001','SUP01','ACTIVE' WHERE NOT EXISTS(SELECT 1 FROM bom_supplier WHERE supplier_code='SUP01');
INSERT INTO bom_supplier(supplier_code,name,sap_vendor,srm_code,status) SELECT 'SUP02','华南动力系统','100002','SUP02','ACTIVE' WHERE NOT EXISTS(SELECT 1 FROM bom_supplier WHERE supplier_code='SUP02');
INSERT INTO bom_supplier(supplier_code,name,sap_vendor,srm_code,status) SELECT 'SUP03','苏州电子制造','100003','SUP03','ACTIVE' WHERE NOT EXISTS(SELECT 1 FROM bom_supplier WHERE supplier_code='SUP03');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,unit_cost,weight_kg,is_phantom) SELECT 'V-M01','A','星耀SUV整车总成','ASSEMBLY','BODY','EA','MAKE','RELEASED',200000,1500,FALSE WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='V-M01' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,unit_cost,weight_kg,is_phantom) SELECT 'P-BODY','A','车身总成','ASSEMBLY','BODY','EA','MAKE','RELEASED',30000,400,FALSE WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-BODY' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,unit_cost,weight_kg,is_phantom) SELECT 'P-POWER','A','动力总成','ASSEMBLY','POWERTRAIN','EA','MAKE','RELEASED',55000,450,FALSE WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-POWER' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,unit_cost,weight_kg,is_phantom)
SELECT 'P-ENGINE15','A','1.5T发动机','SUB_ASSEMBLY','POWERTRAIN','EA','BUY','RELEASED',18000,180,FALSE
WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-ENGINE15' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,unit_cost,weight_kg,is_phantom)
SELECT 'P-ENGINE20','A','2.0T发动机','SUB_ASSEMBLY','POWERTRAIN','EA','BUY','RELEASED',23000,210,FALSE
WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-ENGINE20' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,unit_cost,weight_kg,is_phantom)
SELECT 'P-GEAR-MT','A','手动变速箱','SUB_ASSEMBLY','POWERTRAIN','EA','BUY','RELEASED',9000,80,FALSE
WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-GEAR-MT' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,unit_cost,weight_kg,is_phantom)
SELECT 'P-GEAR-AT','A','自动变速箱','SUB_ASSEMBLY','POWERTRAIN','EA','BUY','RELEASED',13000,95,FALSE
WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-GEAR-AT' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,unit_cost,weight_kg,is_phantom) SELECT 'P-DOOR-FL','A','左前车门','PART','BODY','EA','BUY','RELEASED',2500,32,FALSE WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-DOOR-FL' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,unit_cost,weight_kg,is_phantom) SELECT 'P-BOLT','A','六角螺栓','STANDARD','CHASSIS','EA','BUY','RELEASED',1.2,.05,FALSE WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-BOLT' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,unit_cost,weight_kg,is_phantom) SELECT 'P-WIRE','A','整车线束','PART','ELECTRICAL','EA','BUY','RELEASED',1200,12,FALSE WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-WIRE' AND revision='A');
INSERT INTO bom_header(bom_no,bom_type,root_part_id,vehicle_model_id,plant_id,version,status,description)
SELECT 'BOM-M01-EBOM','EBOM',(SELECT id FROM bom_part WHERE part_no='V-M01'),
       (SELECT id FROM bom_vehicle_model WHERE model_code='M01'),
       (SELECT id FROM bom_plant WHERE plant_code='P001'),1,'RELEASED','M01示范工程BOM'
WHERE NOT EXISTS(SELECT 1 FROM bom_header WHERE bom_no='BOM-M01-EBOM');
UPDATE bom_header SET plant_id = (SELECT id FROM bom_plant WHERE plant_code='P001')
WHERE bom_no='BOM-M01-EBOM' AND plant_id IS NULL;
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,h.root_part_id,p.id,10,1,'EA','NORMAL',NULL
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM' AND h.version=1 AND p.part_no='P-BODY'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND child_part_id=p.id);
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,h.root_part_id,p.id,20,1,'EA','NORMAL',NULL
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM' AND h.version=1 AND p.part_no='P-POWER'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND child_part_id=p.id);
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,(SELECT id FROM bom_part WHERE part_no='P-POWER'),p.id,10,1,'EA','OPTIONAL','ENGINE=1.5T'
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM' AND h.version=1 AND p.part_no='P-ENGINE15'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND child_part_id=p.id);
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,(SELECT id FROM bom_part WHERE part_no='P-POWER'),p.id,20,1,'EA','OPTIONAL','ENGINE=2.0T'
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM' AND h.version=1 AND p.part_no='P-ENGINE20'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND child_part_id=p.id);
MERGE INTO bom_feature KEY(feature,vehicle_model_id) VALUES (1,(SELECT id FROM bom_vehicle_model WHERE model_code='M01'),'ENGINE','发动机');
MERGE INTO bom_feature KEY(feature,vehicle_model_id) VALUES (2,(SELECT id FROM bom_vehicle_model WHERE model_code='M01'),'TRANS','变速箱');
MERGE INTO bom_feature KEY(feature,vehicle_model_id) VALUES (3,(SELECT id FROM bom_vehicle_model WHERE model_code='M01'),'COLOR','颜色');
MERGE INTO bom_feature KEY(feature,vehicle_model_id) VALUES (4,(SELECT id FROM bom_vehicle_model WHERE model_code='M01'),'DRIVE','驱动');
MERGE INTO bom_feature_option KEY(feature_id,option_code) VALUES (1,'1.5T','1.5T');
MERGE INTO bom_feature_option KEY(feature_id,option_code) VALUES (1,'2.0T','2.0T');
MERGE INTO bom_feature_option KEY(feature_id,option_code) VALUES (2,'MT','MT');
MERGE INTO bom_feature_option KEY(feature_id,option_code) VALUES (2,'AT','AT');
MERGE INTO bom_feature_option KEY(feature_id,option_code) VALUES (3,'WHT','白色');
MERGE INTO bom_feature_option KEY(feature_id,option_code) VALUES (3,'BLK','黑色');
MERGE INTO bom_feature_option KEY(feature_id,option_code) VALUES (3,'RED','红色');
MERGE INTO bom_feature_option KEY(feature_id,option_code) VALUES (4,'2WD','两驱');
MERGE INTO bom_feature_option KEY(feature_id,option_code) VALUES (4,'4WD','四驱');
MERGE INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom,safety_part,regulatory_part) KEY(part_no,revision) VALUES ('P-CHASSIS','A','底盘总成','ASSEMBLY','CHASSIS','EA','MAKE','RELEASED',FALSE,FALSE,FALSE);
MERGE INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom,safety_part,regulatory_part) KEY(part_no,revision) VALUES ('P-ELEC','A','电器总成','ASSEMBLY','ELECTRICAL','EA','MAKE','RELEASED',FALSE,FALSE,FALSE);
MERGE INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom,safety_part,regulatory_part) KEY(part_no,revision) VALUES ('P-INTERIOR','A','内饰总成','ASSEMBLY','INTERIOR','EA','MAKE','RELEASED',FALSE,FALSE,FALSE);
MERGE INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom,safety_part,regulatory_part) KEY(part_no,revision) VALUES ('P-BUMPER-F','A','前保险杠','PART','EXTERIOR','EA','BUY','RELEASED',FALSE,FALSE,FALSE);
MERGE INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom,safety_part,regulatory_part) KEY(part_no,revision) VALUES ('P-BUMPER-R','A','后保险杠','PART','EXTERIOR','EA','BUY','RELEASED',FALSE,FALSE,FALSE);
MERGE INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom,safety_part,regulatory_part) KEY(part_no,revision) VALUES ('P-DOOR-FR','A','右前车门','PART','BODY','EA','BUY','RELEASED',FALSE,FALSE,FALSE);
MERGE INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom,safety_part,regulatory_part) KEY(part_no,revision) VALUES ('P-DOOR-RL','A','左后车门','PART','BODY','EA','BUY','RELEASED',FALSE,FALSE,FALSE);
MERGE INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom,safety_part,regulatory_part) KEY(part_no,revision) VALUES ('P-DOOR-RR','A','右后车门','PART','BODY','EA','BUY','RELEASED',FALSE,FALSE,FALSE);
MERGE INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom,safety_part,regulatory_part) KEY(part_no,revision) VALUES ('P-SEAT-F','A','前排座椅','PART','INTERIOR','EA','BUY','RELEASED',FALSE,FALSE,FALSE);
MERGE INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom,safety_part,regulatory_part) KEY(part_no,revision) VALUES ('P-SEAT-R','A','后排座椅','PART','INTERIOR','EA','BUY','RELEASED',FALSE,FALSE,FALSE);
MERGE INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom,safety_part,regulatory_part) KEY(part_no,revision) VALUES ('P-BATTERY','A','动力电池','PART','ELECTRICAL','EA','BUY','RELEASED',FALSE,TRUE,FALSE);
MERGE INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom,safety_part,regulatory_part) KEY(part_no,revision) VALUES ('P-ALTERNATOR','A','发电机','PART','ELECTRICAL','EA','BUY','RELEASED',FALSE,FALSE,FALSE);
MERGE INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom,safety_part,regulatory_part) KEY(part_no,revision) VALUES ('P-ECU','A','发动机控制器','SOFTWARE','ELECTRICAL','EA','MAKE','RELEASED',FALSE,FALSE,TRUE);
MERGE INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom,safety_part,regulatory_part) KEY(part_no,revision) VALUES ('P-TIRE-FL','A','左前轮胎','PART','CHASSIS','EA','BUY','RELEASED',FALSE,FALSE,FALSE);
MERGE INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom,safety_part,regulatory_part) KEY(part_no,revision) VALUES ('P-TIRE-FR','A','右前轮胎','PART','CHASSIS','EA','BUY','RELEASED',FALSE,FALSE,FALSE);
MERGE INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom,safety_part,regulatory_part) KEY(part_no,revision) VALUES ('P-TIRE-RL','A','左后轮胎','PART','CHASSIS','EA','BUY','RELEASED',FALSE,FALSE,FALSE);
MERGE INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom,safety_part,regulatory_part) KEY(part_no,revision) VALUES ('P-TIRE-RR','A','右后轮胎','PART','CHASSIS','EA','BUY','RELEASED',FALSE,FALSE,FALSE);
MERGE INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom,safety_part,regulatory_part) KEY(part_no,revision) VALUES ('P-WHEEL','A','铝合金轮毂','PART','CHASSIS','EA','BUY','RELEASED',FALSE,FALSE,FALSE);
MERGE INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom,safety_part,regulatory_part) KEY(part_no,revision) VALUES ('P-BRAKE','A','制动器总成','SUB_ASSEMBLY','CHASSIS','EA','BUY','RELEASED',FALSE,TRUE,FALSE);
MERGE INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom,safety_part,regulatory_part) KEY(part_no,revision) VALUES ('P-STEERING','A','转向系统','SUB_ASSEMBLY','CHASSIS','EA','BUY','RELEASED',FALSE,TRUE,FALSE);
MERGE INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom,safety_part,regulatory_part) KEY(part_no,revision) VALUES ('P-AIRBAG','A','安全气囊','STANDARD','INTERIOR','EA','BUY','RELEASED',FALSE,TRUE,TRUE);
MERGE INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom,safety_part,regulatory_part) KEY(part_no,revision) VALUES ('P-GLASS-F','A','前挡风玻璃','PART','EXTERIOR','EA','BUY','RELEASED',FALSE,TRUE,FALSE);
MERGE INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom,safety_part,regulatory_part) KEY(part_no,revision) VALUES ('P-GLASS-R','A','后挡风玻璃','PART','EXTERIOR','EA','BUY','RELEASED',FALSE,TRUE,FALSE);
MERGE INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom,safety_part,regulatory_part) KEY(part_no,revision) VALUES ('P-MIRROR','A','外后视镜','PART','EXTERIOR','EA','BUY','RELEASED',FALSE,FALSE,FALSE);
MERGE INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom,safety_part,regulatory_part) KEY(part_no,revision) VALUES ('P-LAMP-F','A','前大灯','PART','EXTERIOR','EA','BUY','RELEASED',FALSE,FALSE,TRUE);
MERGE INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom,safety_part,regulatory_part) KEY(part_no,revision) VALUES ('P-LAMP-R','A','后尾灯','PART','EXTERIOR','EA','BUY','RELEASED',FALSE,FALSE,TRUE);
MERGE INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom,safety_part,regulatory_part) KEY(part_no,revision) VALUES ('P-DASH','A','仪表板','PART','INTERIOR','EA','MAKE','RELEASED',FALSE,FALSE,FALSE);
MERGE INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom,safety_part,regulatory_part) KEY(part_no,revision) VALUES ('P-CARPET','A','地毯','RAW','INTERIOR','EA','BUY','RELEASED',FALSE,FALSE,FALSE);
MERGE INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom,safety_part,regulatory_part) KEY(part_no,revision) VALUES ('P-PAINT','A','车身涂料','RAW','EXTERIOR','KG','BUY','RELEASED',FALSE,FALSE,FALSE);
MERGE INTO bom_work_station KEY(station_code) VALUES (1,(SELECT id FROM bom_plant WHERE plant_code='P001'),'FA','FA010','车身上线',10,60);
MERGE INTO bom_work_station KEY(station_code) VALUES (2,(SELECT id FROM bom_plant WHERE plant_code='P001'),'FA','FA020','底盘装配',20,60);
MERGE INTO bom_work_station KEY(station_code) VALUES (3,(SELECT id FROM bom_plant WHERE plant_code='P001'),'FA','FA030','动力总成',30,60);
MERGE INTO bom_work_station KEY(station_code) VALUES (4,(SELECT id FROM bom_plant WHERE plant_code='P001'),'FA','FA040','内饰装配',40,60);
MERGE INTO bom_work_station KEY(station_code) VALUES (5,(SELECT id FROM bom_plant WHERE plant_code='P001'),'FA','FA050','电器检测',50,60);
MERGE INTO bom_work_station KEY(station_code) VALUES (6,(SELECT id FROM bom_plant WHERE plant_code='P001'),'FA','FA060','终检下线',60,60);
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle,is_phantom)
SELECT 'P-HARNESS-PACK','A','线束包','SUB_ASSEMBLY','ELECTRICAL','EA','MAKE','RELEASED',TRUE
WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-HARNESS-PACK' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle)
SELECT 'P-DOOR-GLASS','A','车门玻璃','PART','BODY','EA','BUY','RELEASED'
WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-DOOR-GLASS' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle)
SELECT 'P-DOOR-LOCK','A','车门锁','PART','BODY','EA','BUY','RELEASED'
WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-DOOR-LOCK' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle)
SELECT 'P-WIRE-CLIP','A','线束固定螺栓','STANDARD','ELECTRICAL','EA','BUY','RELEASED'
WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-WIRE-CLIP' AND revision='A');
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,h.root_part_id,p.id,30,1,'EA','NORMAL',NULL
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM' AND h.version=1 AND p.part_no='P-CHASSIS'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND child_part_id=p.id);
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,h.root_part_id,p.id,40,1,'EA','NORMAL',NULL
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM' AND h.version=1 AND p.part_no='P-ELEC'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND child_part_id=p.id);
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,h.root_part_id,p.id,50,1,'EA','NORMAL',NULL
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM' AND h.version=1 AND p.part_no='P-INTERIOR'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND child_part_id=p.id);
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,(SELECT id FROM bom_part WHERE part_no='P-BODY'),p.id,10,1,'EA','NORMAL',NULL
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM' AND h.version=1 AND p.part_no='P-BUMPER-F'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND child_part_id=p.id);
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,(SELECT id FROM bom_part WHERE part_no='P-BODY'),p.id,20,1,'EA','NORMAL',NULL
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM' AND h.version=1 AND p.part_no='P-BUMPER-R'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND child_part_id=p.id);
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,(SELECT id FROM bom_part WHERE part_no='P-BODY'),p.id,30,1,'EA','NORMAL',NULL
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM' AND h.version=1 AND p.part_no='P-DOOR-FL'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND child_part_id=p.id);
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,(SELECT id FROM bom_part WHERE part_no='P-BODY'),p.id,40,1,'EA','NORMAL',NULL
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM' AND h.version=1 AND p.part_no='P-DOOR-FR'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND child_part_id=p.id);
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,(SELECT id FROM bom_part WHERE part_no='P-BODY'),p.id,50,1,'EA','NORMAL',NULL
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM' AND h.version=1 AND p.part_no='P-DOOR-RL'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND child_part_id=p.id);
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,(SELECT id FROM bom_part WHERE part_no='P-BODY'),p.id,60,1,'EA','NORMAL',NULL
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM' AND h.version=1 AND p.part_no='P-DOOR-RR'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND child_part_id=p.id);
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,(SELECT id FROM bom_part WHERE part_no='P-DOOR-FL'),p.id,10,1,'EA','NORMAL',NULL
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM' AND h.version=1 AND p.part_no='P-DOOR-GLASS'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND parent_part_id=(SELECT id FROM bom_part WHERE part_no='P-DOOR-FL') AND child_part_id=p.id);
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,(SELECT id FROM bom_part WHERE part_no='P-DOOR-FL'),p.id,20,1,'EA','NORMAL',NULL
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM' AND h.version=1 AND p.part_no='P-DOOR-LOCK'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND parent_part_id=(SELECT id FROM bom_part WHERE part_no='P-DOOR-FL') AND child_part_id=p.id);
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,(SELECT id FROM bom_part WHERE part_no='P-DOOR-GLASS'),p.id,10,4,'EA','NORMAL',NULL
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM' AND h.version=1 AND p.part_no='P-BOLT'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND parent_part_id=(SELECT id FROM bom_part WHERE part_no='P-DOOR-GLASS') AND child_part_id=p.id);
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,(SELECT id FROM bom_part WHERE part_no='P-CHASSIS'),p.id,10,4,'EA','NORMAL',NULL
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM' AND h.version=1 AND p.part_no='P-WHEEL'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND child_part_id=p.id);
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,(SELECT id FROM bom_part WHERE part_no='P-CHASSIS'),p.id,20,4,'EA','NORMAL',NULL
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM' AND h.version=1 AND p.part_no='P-TIRE-FL'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND child_part_id=p.id);
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,(SELECT id FROM bom_part WHERE part_no='P-CHASSIS'),p.id,30,1,'EA','NORMAL',NULL
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM' AND h.version=1 AND p.part_no='P-BRAKE'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND child_part_id=p.id);
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,(SELECT id FROM bom_part WHERE part_no='P-POWER'),p.id,30,1,'EA','NORMAL',NULL
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM' AND h.version=1 AND p.part_no='P-FUEL-TANK'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND child_part_id=p.id);
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,(SELECT id FROM bom_part WHERE part_no='P-POWER'),p.id,40,1,'EA','OPTIONAL','TRANS=MT'
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM' AND h.version=1 AND p.part_no='P-GEAR-MT'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND child_part_id=p.id);
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,(SELECT id FROM bom_part WHERE part_no='P-POWER'),p.id,50,1,'EA','OPTIONAL','TRANS=AT'
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM' AND h.version=1 AND p.part_no='P-GEAR-AT'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND child_part_id=p.id);
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,(SELECT id FROM bom_part WHERE part_no='P-ELEC'),p.id,10,1,'EA','NORMAL',NULL
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM' AND h.version=1 AND p.part_no='P-BATTERY'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND child_part_id=p.id);
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,(SELECT id FROM bom_part WHERE part_no='P-ELEC'),p.id,20,1,'EA','NORMAL',NULL
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM' AND h.version=1 AND p.part_no='P-ECU'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND child_part_id=p.id);
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,(SELECT id FROM bom_part WHERE part_no='P-ELEC'),p.id,30,1,'EA','PHANTOM',NULL
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM' AND h.version=1 AND p.part_no='P-HARNESS-PACK'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND child_part_id=p.id);
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,(SELECT id FROM bom_part WHERE part_no='P-INTERIOR'),p.id,10,2,'EA','NORMAL',NULL
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM' AND h.version=1 AND p.part_no='P-SEAT-F'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND child_part_id=p.id);
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,(SELECT id FROM bom_part WHERE part_no='P-INTERIOR'),p.id,20,1,'EA','NORMAL',NULL
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM' AND h.version=1 AND p.part_no='P-SEAT-R'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND child_part_id=p.id);
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,(SELECT id FROM bom_part WHERE part_no='P-INTERIOR'),p.id,30,1,'EA','NORMAL',NULL
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM' AND h.version=1 AND p.part_no='P-DASH'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND child_part_id=p.id);
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,(SELECT id FROM bom_part WHERE part_no='P-HARNESS-PACK'),p.id,10,1,'EA','NORMAL',NULL
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM' AND h.version=1 AND p.part_no='P-WIRE'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND child_part_id=p.id);
MERGE INTO bom_ecr KEY(ecr_no) VALUES (1,'ECR-DEMO-001','发动机供应商替换','SUPPLY','HIGH',(SELECT id FROM bom_vehicle_model WHERE model_code='M01'),'[]','演示工程变更','APPROVED','admin','admin',CURRENT_TIMESTAMP,NULL);
MERGE INTO bom_ecn KEY(ecn_no) VALUES (1,'ECN-DEMO-001',1,'自动变速箱替换',(SELECT id FROM bom_header WHERE bom_no='BOM-M01-EBOM' AND version=1),'REPLACE','IMMEDIATE',NULL,NULL,'DRAFT',NULL);
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-CHASSIS','A','底盘总成','ASSEMBLY','CHASSIS','EA','MAKE','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-CHASSIS' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-ELEC','A','电器总成','ASSEMBLY','ELECTRICAL','EA','MAKE','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-ELEC' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-INTERIOR','A','内饰总成','ASSEMBLY','INTERIOR','EA','MAKE','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-INTERIOR' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-BUMPER-F','A','前保险杠','PART','EXTERIOR','EA','BUY','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-BUMPER-F' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-BUMPER-R','A','后保险杠','PART','EXTERIOR','EA','BUY','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-BUMPER-R' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-DOOR-FR','A','右前车门','PART','BODY','EA','BUY','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-DOOR-FR' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-DOOR-RL','A','左后车门','PART','BODY','EA','BUY','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-DOOR-RL' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-DOOR-RR','A','右后车门','PART','BODY','EA','BUY','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-DOOR-RR' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-SEAT-F','A','前排座椅','PART','INTERIOR','EA','BUY','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-SEAT-F' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-SEAT-R','A','后排座椅','PART','INTERIOR','EA','BUY','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-SEAT-R' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-BATTERY','A','动力电池','PART','ELECTRICAL','EA','BUY','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-BATTERY' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-ALTERNATOR','A','发电机','PART','ELECTRICAL','EA','BUY','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-ALTERNATOR' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-ECU','A','发动机控制器','SOFTWARE','ELECTRICAL','EA','MAKE','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-ECU' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-TIRE-FL','A','左前轮胎','PART','CHASSIS','EA','BUY','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-TIRE-FL' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-TIRE-FR','A','右前轮胎','PART','CHASSIS','EA','BUY','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-TIRE-FR' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-TIRE-RL','A','左后轮胎','PART','CHASSIS','EA','BUY','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-TIRE-RL' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-TIRE-RR','A','右后轮胎','PART','CHASSIS','EA','BUY','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-TIRE-RR' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-WHEEL','A','铝合金轮毂','PART','CHASSIS','EA','BUY','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-WHEEL' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-BRAKE','A','制动器总成','SUB_ASSEMBLY','CHASSIS','EA','BUY','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-BRAKE' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-STEERING','A','转向系统','SUB_ASSEMBLY','CHASSIS','EA','BUY','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-STEERING' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-AIRBAG','A','安全气囊','STANDARD','INTERIOR','EA','BUY','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-AIRBAG' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-GLASS-F','A','前挡风玻璃','PART','EXTERIOR','EA','BUY','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-GLASS-F' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-GLASS-R','A','后挡风玻璃','PART','EXTERIOR','EA','BUY','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-GLASS-R' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-MIRROR','A','外后视镜','PART','EXTERIOR','EA','BUY','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-MIRROR' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-LAMP-F','A','前大灯','PART','EXTERIOR','EA','BUY','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-LAMP-F' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-LAMP-R','A','后尾灯','PART','EXTERIOR','EA','BUY','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-LAMP-R' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-DASH','A','仪表板','PART','INTERIOR','EA','MAKE','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-DASH' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-CARPET','A','地毯','RAW','INTERIOR','EA','BUY','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-CARPET' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-PAINT','A','车身涂料','RAW','EXTERIOR','KG','BUY','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-PAINT' AND revision='A');
INSERT INTO bom_part(part_no,revision,part_name,part_type,category,uom,make_buy,lifecycle) SELECT 'P-FUEL-TANK','A','燃油箱','PART','POWERTRAIN','EA','BUY','RELEASED' WHERE NOT EXISTS(SELECT 1 FROM bom_part WHERE part_no='P-FUEL-TANK' AND revision='A');
UPDATE bom_part
SET material='汽车级材料',
    weight_kg=1,
    unit_cost=10,
    lead_time_days=15,
    drawing_no=CONCAT('DWG-',part_no)
WHERE part_no LIKE 'P-%'
   OR part_no='V-M01';
UPDATE bom_part
SET supplier_id=(SELECT id FROM bom_supplier WHERE supplier_code='SUP01')
WHERE make_buy='BUY';
UPDATE bom_part
SET material='车身钢板',
    weight_kg=400,
    unit_cost=30000,
    lead_time_days=20,
    drawing_no='DWG-P-BODY'
WHERE part_no='P-BODY';
UPDATE bom_part
SET material='铝合金及钢材',
    weight_kg=450,
    unit_cost=55000,
    lead_time_days=25,
    drawing_no='DWG-P-POWER'
WHERE part_no='P-POWER';
UPDATE bom_part
SET material='铝合金',
    weight_kg=120,
    unit_cost=15000,
    lead_time_days=30,
    drawing_no='DWG-P-ENGINE15'
WHERE part_no='P-ENGINE15';
UPDATE bom_part
SET material='铝合金',
    weight_kg=165,
    unit_cost=22000,
    lead_time_days=35,
    drawing_no='DWG-P-ENGINE20'
WHERE part_no='P-ENGINE20';
UPDATE bom_part
SET material='铝合金壳体',
    weight_kg=82,
    unit_cost=9000,
    lead_time_days=30,
    drawing_no='DWG-P-GEAR-MT'
WHERE part_no='P-GEAR-MT';
UPDATE bom_part
SET material='铝合金壳体',
    weight_kg=88,
    unit_cost=13000,
    lead_time_days=30,
    drawing_no='DWG-P-GEAR-AT'
WHERE part_no='P-GEAR-AT';
UPDATE bom_part
SET material='镀锌钢',
    weight_kg=.02,
    unit_cost=.5,
    lead_time_days=7,
    drawing_no='STD-P-BOLT'
WHERE part_no='P-BOLT';
UPDATE bom_part
SET material='钢板及玻璃',
    weight_kg=32,
    unit_cost=2500,
    lead_time_days=20,
    drawing_no=CONCAT('DWG-',part_no)
WHERE part_no LIKE 'P-DOOR-%';
UPDATE bom_part
SET material='钢材',
    weight_kg=1500,
    unit_cost=200000,
    lead_time_days=45,
    drawing_no='DWG-V-M01'
WHERE part_no='V-M01';
INSERT INTO bom_feature(vehicle_model_id,feature,name) SELECT (SELECT id FROM bom_vehicle_model WHERE model_code='M01'),'ENGINE','发动机' WHERE NOT EXISTS(SELECT 1 FROM bom_feature WHERE feature='ENGINE');
INSERT INTO bom_feature(vehicle_model_id,feature,name) SELECT (SELECT id FROM bom_vehicle_model WHERE model_code='M01'),'TRANS','变速箱' WHERE NOT EXISTS(SELECT 1 FROM bom_feature WHERE feature='TRANS');
INSERT INTO bom_feature_option(feature_id,option_code,option_name) SELECT (SELECT id FROM bom_feature WHERE feature='ENGINE'),'1.5T','1.5T' WHERE NOT EXISTS(SELECT 1 FROM bom_feature_option WHERE option_code='1.5T');
INSERT INTO bom_feature_option(feature_id,option_code,option_name) SELECT (SELECT id FROM bom_feature WHERE feature='ENGINE'),'2.0T','2.0T' WHERE NOT EXISTS(SELECT 1 FROM bom_feature_option WHERE option_code='2.0T');
INSERT INTO bom_feature_option(feature_id,option_code,option_name) SELECT (SELECT id FROM bom_feature WHERE feature='TRANS'),'MT','MT' WHERE NOT EXISTS(SELECT 1 FROM bom_feature_option WHERE option_code='MT');
INSERT INTO bom_feature_option(feature_id,option_code,option_name) SELECT (SELECT id FROM bom_feature WHERE feature='TRANS'),'AT','AT' WHERE NOT EXISTS(SELECT 1 FROM bom_feature_option WHERE option_code='AT');
INSERT INTO bom_ecr(ecr_no,title,reason,priority,vehicle_model_id,affected_part_ids,description,status,requester,approved_by,approved_at)
SELECT 'ECR-DEMO-001','发动机供应商替换','SUPPLY','HIGH',
       (SELECT id FROM bom_vehicle_model WHERE model_code='M01'),'[]','演示工程变更',
       'APPROVED','admin','admin',CURRENT_TIMESTAMP
WHERE NOT EXISTS(SELECT 1 FROM bom_ecr WHERE ecr_no='ECR-DEMO-001');
INSERT INTO bom_ecn(ecn_no,ecr_id,title,bom_id,change_type,effective_type,status)
SELECT 'ECN-DEMO-001',e.id,'自动变速箱替换',
       (SELECT id FROM bom_header WHERE bom_no='BOM-M01-EBOM' AND version=1),
       'REPLACE','IMMEDIATE','DRAFT'
FROM bom_ecr e
WHERE e.ecr_no='ECR-DEMO-001'
  AND NOT EXISTS(SELECT 1 FROM bom_ecn WHERE ecn_no='ECN-DEMO-001');
INSERT INTO bom_ecn_item(
    ecn_id,
    action,
    parent_part_id,
    old_child_part_id,
    new_child_part_id,
    old_qty,
    new_qty,
    find_no,
    old_usage_condition,
    usage_condition,
    remark
)
SELECT e.id,
       'REPLACE',
       (SELECT id FROM bom_part WHERE part_no='P-POWER'),
       (SELECT id FROM bom_part WHERE part_no='P-GEAR-MT'),
       (SELECT id FROM bom_part WHERE part_no='P-GEAR-AT'),
       1,
       1,
       40,
       'TRANS=MT',
       NULL,
       '演示变速箱替换'
FROM bom_ecn e
WHERE e.ecn_no='ECN-DEMO-001'
  AND NOT EXISTS(
      SELECT 1
      FROM bom_ecn_item
      WHERE ecn_id=e.id
        AND action='REPLACE'
        AND parent_part_id=(SELECT id FROM bom_part WHERE part_no='P-POWER')
        AND old_child_part_id=(SELECT id FROM bom_part WHERE part_no='P-GEAR-MT')
        AND new_child_part_id=(SELECT id FROM bom_part WHERE part_no='P-GEAR-AT')
  );
INSERT INTO bom_ecn(ecn_no,ecr_id,title,bom_id,change_type,effective_type,status)
SELECT 'ECN-IR-SUBMITTED',e.id,'控制塔待批准工程变更',
       (SELECT id FROM bom_header WHERE bom_no='BOM-M01-EBOM' AND version=1),
       'REPLACE','IMMEDIATE','SUBMITTED'
FROM bom_ecr e
WHERE e.ecr_no='ECR-DEMO-001'
  AND NOT EXISTS(SELECT 1 FROM bom_ecn WHERE ecn_no='ECN-IR-SUBMITTED');
INSERT INTO bom_header(bom_no,bom_type,root_part_id,vehicle_model_id,plant_id,version,status,description,source_bom_id)
SELECT 'BOM-M01-MBOM','MBOM',(SELECT id FROM bom_part WHERE part_no='V-M01'),
       (SELECT id FROM bom_vehicle_model WHERE model_code='M01'),
       (SELECT id FROM bom_plant WHERE plant_code='P001'),1,'RELEASED',
       'M01示范制造BOM',(SELECT id FROM bom_header WHERE bom_no='BOM-M01-EBOM' AND version=1)
WHERE NOT EXISTS(SELECT 1 FROM bom_header WHERE bom_no='BOM-M01-MBOM');
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,station_code)
SELECT h.id,h.root_part_id,p.id,10,1,'EA','NORMAL','FA010'
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-MBOM' AND h.version=1 AND p.part_no='P-BODY'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND child_part_id=p.id);
INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,station_code)
SELECT h.id,h.root_part_id,p.id,20,1,'EA','NORMAL','FA030'
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-MBOM' AND h.version=1 AND p.part_no='P-POWER'
  AND NOT EXISTS(SELECT 1 FROM bom_item WHERE bom_id=h.id AND child_part_id=p.id);
INSERT INTO bom_header(bom_no,bom_type,root_part_id,vehicle_model_id,version,status,description,source_bom_id)
SELECT 'BOM-M01-EBOM-V2','EBOM',(SELECT id FROM bom_part WHERE part_no='V-M01'),
       (SELECT id FROM bom_vehicle_model WHERE model_code='M01'),2,'DRAFT',
       'M01对比演示版本',(SELECT id FROM bom_header WHERE bom_no='BOM-M01-EBOM' AND version=1)
WHERE NOT EXISTS(SELECT 1 FROM bom_header WHERE bom_no='BOM-M01-EBOM-V2');
INSERT INTO bom_work_station(plant_id,line_code,station_code,station_name,seq,takt) SELECT p.id,'FA','FA010','车身上线',10,60 FROM bom_plant p WHERE p.plant_code='P001' AND NOT EXISTS(SELECT 1 FROM bom_work_station WHERE station_code='FA010');
INSERT INTO bom_work_station(plant_id,line_code,station_code,station_name,seq,takt) SELECT p.id,'FA','FA020','底盘装配',20,60 FROM bom_plant p WHERE p.plant_code='P001' AND NOT EXISTS(SELECT 1 FROM bom_work_station WHERE station_code='FA020');
INSERT INTO bom_work_station(plant_id,line_code,station_code,station_name,seq,takt) SELECT p.id,'FA','FA030','动力总成',30,60 FROM bom_plant p WHERE p.plant_code='P001' AND NOT EXISTS(SELECT 1 FROM bom_work_station WHERE station_code='FA030');
INSERT INTO bom_work_station(plant_id,line_code,station_code,station_name,seq,takt) SELECT p.id,'FA','FA040','内饰装配',40,60 FROM bom_plant p WHERE p.plant_code='P001' AND NOT EXISTS(SELECT 1 FROM bom_work_station WHERE station_code='FA040');
INSERT INTO bom_work_station(plant_id,line_code,station_code,station_name,seq,takt) SELECT p.id,'FA','FA050','电器检测',50,60 FROM bom_plant p WHERE p.plant_code='P001' AND NOT EXISTS(SELECT 1 FROM bom_work_station WHERE station_code='FA050');
INSERT INTO bom_work_station(plant_id,line_code,station_code,station_name,seq,takt) SELECT p.id,'FA','FA060','终检下线',60,60 FROM bom_plant p WHERE p.plant_code='P001' AND NOT EXISTS(SELECT 1 FROM bom_work_station WHERE station_code='FA060');

INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type)
SELECT h.id,h.root_part_id,p.id,10,2,'EA','NORMAL'
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM-V2' AND p.part_no='P-BODY'
  AND NOT EXISTS(
      SELECT 1
      FROM bom_item
      WHERE bom_id=h.id
        AND parent_part_id=h.root_part_id
        AND child_part_id=p.id
  );

UPDATE bom_part
SET lifecycle='RELEASED'
WHERE id IN (
    SELECT child_part_id
    FROM bom_item
    WHERE child_part_id IS NOT NULL
);

INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type)
SELECT h.id,h.root_part_id,p.id,20,1,'EA','NORMAL'
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM-V2' AND p.part_no='P-MIRROR'
  AND NOT EXISTS(
      SELECT 1
      FROM bom_item
      WHERE bom_id=h.id
        AND parent_part_id=h.root_part_id
        AND child_part_id=p.id
  );

INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,usage_condition)
SELECT h.id,h.root_part_id,p.id,30,1,'EA','OPTIONAL','ENGINE=2.0T'
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-EBOM-V2' AND p.part_no='P-ENGINE20'
  AND NOT EXISTS(
      SELECT 1
      FROM bom_item
      WHERE bom_id=h.id
        AND child_part_id=p.id
  );

INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,station_code)
SELECT h.id,h.root_part_id,p.id,10,1,'EA','NORMAL','FA010'
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-MBOM' AND h.version=1 AND p.part_no='P-BODY'
  AND NOT EXISTS(
      SELECT 1
      FROM bom_item
      WHERE bom_id=h.id
        AND child_part_id=p.id
  );

INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,station_code)
SELECT h.id,h.root_part_id,p.id,20,1,'EA','NORMAL','FA020'
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-MBOM' AND h.version=1 AND p.part_no='P-CHASSIS'
  AND NOT EXISTS(
      SELECT 1
      FROM bom_item
      WHERE bom_id=h.id
        AND child_part_id=p.id
  );

INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,station_code)
SELECT h.id,h.root_part_id,p.id,30,1,'EA','NORMAL','FA030'
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-MBOM' AND h.version=1 AND p.part_no='P-POWER'
  AND NOT EXISTS(
      SELECT 1
      FROM bom_item
      WHERE bom_id=h.id
        AND child_part_id=p.id
  );

INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,station_code)
SELECT h.id,h.root_part_id,p.id,40,1,'EA','NORMAL','FA050'
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-MBOM' AND h.version=1 AND p.part_no='P-ELEC'
  AND NOT EXISTS(
      SELECT 1
      FROM bom_item
      WHERE bom_id=h.id
        AND child_part_id=p.id
  );

INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,station_code)
SELECT h.id,h.root_part_id,p.id,50,1,'EA','NORMAL','FA040'
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-MBOM' AND h.version=1 AND p.part_no='P-INTERIOR'
  AND NOT EXISTS(
      SELECT 1
      FROM bom_item
      WHERE bom_id=h.id
        AND child_part_id=p.id
  );

INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,station_code)
SELECT h.id,(SELECT id FROM bom_part WHERE part_no='P-BODY'),p.id,10,1,'EA','NORMAL','FA010'
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-MBOM' AND h.version=1 AND p.part_no='P-BUMPER-F'
  AND NOT EXISTS(
      SELECT 1
      FROM bom_item
      WHERE bom_id=h.id
        AND parent_part_id=(SELECT id FROM bom_part WHERE part_no='P-BODY')
        AND child_part_id=p.id
  );

INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,station_code)
SELECT h.id,(SELECT id FROM bom_part WHERE part_no='P-BODY'),p.id,20,1,'EA','NORMAL','FA010'
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-MBOM' AND h.version=1 AND p.part_no='P-BUMPER-R'
  AND NOT EXISTS(
      SELECT 1
      FROM bom_item
      WHERE bom_id=h.id
        AND parent_part_id=(SELECT id FROM bom_part WHERE part_no='P-BODY')
        AND child_part_id=p.id
  );

INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,station_code)
SELECT h.id,(SELECT id FROM bom_part WHERE part_no='P-POWER'),p.id,10,1,'EA','OPTIONAL','FA030'
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-MBOM' AND h.version=1 AND p.part_no='P-ENGINE15'
  AND NOT EXISTS(
      SELECT 1
      FROM bom_item
      WHERE bom_id=h.id
        AND parent_part_id=(SELECT id FROM bom_part WHERE part_no='P-POWER')
        AND child_part_id=p.id
  );

INSERT INTO bom_item(bom_id,parent_part_id,child_part_id,find_no,qty,uom,usage_type,station_code)
SELECT h.id,(SELECT id FROM bom_part WHERE part_no='P-POWER'),p.id,20,1,'EA','OPTIONAL','FA030'
FROM bom_header h,bom_part p
WHERE h.bom_no='BOM-M01-MBOM' AND h.version=1 AND p.part_no='P-GEAR-AT'
  AND NOT EXISTS(
      SELECT 1
      FROM bom_item
      WHERE bom_id=h.id
        AND parent_part_id=(SELECT id FROM bom_part WHERE part_no='P-POWER')
        AND child_part_id=p.id
  );
