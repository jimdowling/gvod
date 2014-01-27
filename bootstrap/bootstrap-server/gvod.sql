CREATE TABLE nodes (
id INT NOT NULL,
ip INT UNSIGNED NOT NULL,
port SMALLINT UNSIGNED NOT NULL,
asn SMALLINT UNSIGNED NOT NULL DEFAULT 0,
country char(2) NOT NULL DEFAULT 'se',
nat_type TINYINT UNSIGNED NOT NULL,
open BOOLEAN NOT NULL,
mtu SMALLINT UNSIGNED NOT NULL DEFAULT 1500,
last_ping TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
joined TIMESTAMP NOT NULL,
PRIMARY KEY(id),
KEY open_stable_idx(open, last_ping),
KEY country_idx(country),
KEY asn_idx(asn)
) engine=innodb;


CREATE TABLE overlay_details (
overlay_id INT NOT NULL,
overlay_name char(128) NOT NULL,
overlay_description VARCHAR(512) NULL,
date_added TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
overlay_picture BLOB NULL,
PRIMARY KEY (overlay_id)
) engine=innodb; 


CREATE TABLE overlays (
id INT NOT NULL,
overlay_id INT NOT NULL,
utility INT NOT NULL,
PRIMARY KEY (id, overlay_id),
KEY overlay_idx (overlay_id),
KEY utility_idx(utility),
CONSTRAINT id_fk FOREIGN KEY (id) REFERENCES nodes(id) ON DELETE CASCADE,
CONSTRAINT overlay_fk FOREIGN KEY (overlay_id) REFERENCES overlay_details(overlay_id) ON DELETE CASCADE
) engine=innodb; 

