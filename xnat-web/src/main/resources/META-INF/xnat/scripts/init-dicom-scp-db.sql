CREATE TABLE dicom_scp_instance (
  id         INT PRIMARY KEY AUTO_INCREMENT,
  ae_title   VARCHAR(255) NOT NULL,
  port       INT          NOT NULL,
  identifier VARCHAR(1024),
  file_namer VARCHAR(1024),
  enabled    BOOLEAN DEFAULT TRUE,
  UNIQUE (ae_title, port)
);

CREATE TABLE dicom_scp (
  id   INT PRIMARY KEY,
  port INT UNIQUE NOT NULL
);

CREATE TABLE dicom_scp_instance_dicom_scp (
  instance_id INT NOT NULL UNIQUE REFERENCES dicom_scp_instance (id),
  scp_id      INT NOT NULL REFERENCES dicom_scp (id)
);
