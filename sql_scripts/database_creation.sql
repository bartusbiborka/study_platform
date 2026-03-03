-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema platforma_de_studiu
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema platforma_de_studiu
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `platforma_de_studiu` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `platforma_de_studiu` ;

-- -----------------------------------------------------
-- Table `platforma_de_studiu`.`cursuri`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `platforma_de_studiu`.`cursuri` (
  `id_cursuri` INT NOT NULL,
  `descriere` TEXT NULL DEFAULT NULL,
  `nr_studnti_maxim` INT NULL DEFAULT NULL,
  `nume` VARCHAR(45) NOT NULL,
  `nr_studenti_inscrisi` INT NULL DEFAULT NULL,
  PRIMARY KEY (`id_cursuri`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `platforma_de_studiu`.`calendar`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `platforma_de_studiu`.`calendar` (
  `id_desfasurare` INT NOT NULL AUTO_INCREMENT,
  `zi` ENUM('luni', 'marti', 'miercuri', 'joi', 'vineri') NULL DEFAULT NULL,
  `data_inceput` TIME NULL DEFAULT NULL,
  `data_sfarsit` TIME NULL DEFAULT NULL,
  `data` DATE NULL DEFAULT NULL,
  PRIMARY KEY (`id_desfasurare`))
ENGINE = InnoDB
AUTO_INCREMENT = 16
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `platforma_de_studiu`.`activitate`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `platforma_de_studiu`.`activitate` (
  `id_ora` INT NOT NULL AUTO_INCREMENT,
  `id_curs` INT NOT NULL,
  `tip` ENUM('curs', 'seminar', 'laborator', 'examen', 'colocviu') NOT NULL,
  `id_desfasurare` INT NULL DEFAULT NULL,
  `nr_max_part` INT NULL DEFAULT NULL,
  PRIMARY KEY (`id_ora`),
  INDEX `fk_curs_idx` (`id_curs` ASC) VISIBLE,
  INDEX `fk_desf_idx` (`id_desfasurare` ASC) VISIBLE,
  CONSTRAINT `fk_curs`
    FOREIGN KEY (`id_curs`)
    REFERENCES `platforma_de_studiu`.`cursuri` (`id_cursuri`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_desf`
    FOREIGN KEY (`id_desfasurare`)
    REFERENCES `platforma_de_studiu`.`calendar` (`id_desfasurare`))
ENGINE = InnoDB
AUTO_INCREMENT = 12
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `platforma_de_studiu`.`grup_de_studi`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `platforma_de_studiu`.`grup_de_studi` (
  `id_grup` INT NOT NULL,
  `nume` VARCHAR(45) NULL DEFAULT NULL,
  `id_curs` INT NULL DEFAULT NULL,
  PRIMARY KEY (`id_grup`),
  INDEX `c_idx` (`id_curs` ASC) VISIBLE,
  CONSTRAINT `c`
    FOREIGN KEY (`id_curs`)
    REFERENCES `platforma_de_studiu`.`cursuri` (`id_cursuri`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `platforma_de_studiu`.`activitate_grup`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `platforma_de_studiu`.`activitate_grup` (
  `id_activitate` INT NOT NULL AUTO_INCREMENT,
  `data_desfasurare` DATE NULL DEFAULT NULL,
  `termen_inscriere` DATETIME NULL DEFAULT NULL,
  `nr_min_participanti` INT NULL DEFAULT NULL,
  `id_prof` INT NULL DEFAULT NULL,
  `nume` TEXT NULL DEFAULT NULL,
  `ora_inceput` TIME NULL DEFAULT NULL,
  `ora_sfarsit` TIME NULL DEFAULT NULL,
  `id_grup` INT NULL DEFAULT NULL,
  `canceled` TINYINT NULL DEFAULT '0',
  PRIMARY KEY (`id_activitate`),
  INDEX `grru_idx` (`id_grup` ASC) VISIBLE,
  CONSTRAINT `grru`
    FOREIGN KEY (`id_grup`)
    REFERENCES `platforma_de_studiu`.`grup_de_studi` (`id_grup`))
ENGINE = InnoDB
AUTO_INCREMENT = 8
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `platforma_de_studiu`.`adresa`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `platforma_de_studiu`.`adresa` (
  `id_adresa` INT NOT NULL,
  `judet` VARCHAR(20) NULL DEFAULT NULL,
  `localitate` VARCHAR(45) NULL DEFAULT NULL,
  `strada` VARCHAR(45) NULL DEFAULT NULL,
  `numar` INT NULL DEFAULT NULL,
  PRIMARY KEY (`id_adresa`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `platforma_de_studiu`.`student`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `platforma_de_studiu`.`student` (
  `id_student` INT NOT NULL,
  `an_de_studiu` INT NULL DEFAULT NULL,
  `nr_ore` INT NULL DEFAULT NULL,
  PRIMARY KEY (`id_student`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `platforma_de_studiu`.`inrolare`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `platforma_de_studiu`.`inrolare` (
  `id_student` INT NOT NULL,
  `id_curs` INT NOT NULL,
  `nota_curs` INT NULL DEFAULT NULL,
  `nota_seminar` INT NULL DEFAULT NULL,
  `nota_lab` INT NULL DEFAULT NULL,
  `nota_finala` DECIMAL(10,2) NULL DEFAULT NULL,
  `id_prof` INT NULL DEFAULT NULL,
  PRIMARY KEY (`id_student`, `id_curs`),
  INDEX `fk_curs4_idx` (`id_curs` ASC) VISIBLE,
  CONSTRAINT `fk_idcurs`
    FOREIGN KEY (`id_curs`)
    REFERENCES `platforma_de_studiu`.`cursuri` (`id_cursuri`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_student`
    FOREIGN KEY (`id_student`)
    REFERENCES `platforma_de_studiu`.`student` (`id_student`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `platforma_de_studiu`.`inrolare_act_grup`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `platforma_de_studiu`.`inrolare_act_grup` (
  `id_activitate` INT NOT NULL,
  `id_student` INT NOT NULL,
  PRIMARY KEY (`id_activitate`, `id_student`),
  INDEX `sd_idx` (`id_student` ASC) VISIBLE,
  CONSTRAINT `av`
    FOREIGN KEY (`id_activitate`)
    REFERENCES `platforma_de_studiu`.`activitate_grup` (`id_activitate`),
  CONSTRAINT `sd`
    FOREIGN KEY (`id_student`)
    REFERENCES `platforma_de_studiu`.`student` (`id_student`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `platforma_de_studiu`.`inrolare_activitati`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `platforma_de_studiu`.`inrolare_activitati` (
  `id_activitate` INT NOT NULL,
  `id_student` INT NOT NULL,
  PRIMARY KEY (`id_activitate`, `id_student`),
  INDEX `stdn_idx` (`id_student` ASC) VISIBLE,
  CONSTRAINT `addd`
    FOREIGN KEY (`id_activitate`)
    REFERENCES `platforma_de_studiu`.`activitate` (`id_ora`),
  CONSTRAINT `stdn`
    FOREIGN KEY (`id_student`)
    REFERENCES `platforma_de_studiu`.`student` (`id_student`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `platforma_de_studiu`.`inrolare_grup_de_studiu`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `platforma_de_studiu`.`inrolare_grup_de_studiu` (
  `id_grup` INT NOT NULL,
  `id_student` INT NOT NULL,
  PRIMARY KEY (`id_grup`, `id_student`),
  INDEX `st_idx` (`id_student` ASC) VISIBLE,
  CONSTRAINT `gr`
    FOREIGN KEY (`id_grup`)
    REFERENCES `platforma_de_studiu`.`grup_de_studi` (`id_grup`),
  CONSTRAINT `st`
    FOREIGN KEY (`id_student`)
    REFERENCES `platforma_de_studiu`.`student` (`id_student`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `platforma_de_studiu`.`mesaj`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `platforma_de_studiu`.`mesaj` (
  `id_mesaj` INT NOT NULL AUTO_INCREMENT,
  `text` VARCHAR(100) NULL DEFAULT NULL,
  `id_grup` INT NULL DEFAULT NULL,
  `id_student` INT NULL DEFAULT NULL,
  `time` TIMESTAMP NOT NULL,
  PRIMARY KEY (`id_mesaj`),
  INDEX `w_idx` (`id_grup` ASC) VISIBLE,
  CONSTRAINT `w`
    FOREIGN KEY (`id_grup`)
    REFERENCES `platforma_de_studiu`.`grup_de_studi` (`id_grup`))
ENGINE = InnoDB
AUTO_INCREMENT = 10
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `platforma_de_studiu`.`profesor`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `platforma_de_studiu`.`profesor` (
  `id_profesor` INT NOT NULL,
  `nr_ore_minim` INT NULL DEFAULT NULL,
  `nr_ore_maxim` INT NULL DEFAULT NULL,
  `departament` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`id_profesor`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `platforma_de_studiu`.`prof_cursuri`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `platforma_de_studiu`.`prof_cursuri` (
  `id_profesor` INT NOT NULL,
  `id_curs` INT NOT NULL,
  PRIMARY KEY (`id_profesor`, `id_curs`),
  INDEX `fk_curs_idx` (`id_curs` ASC) VISIBLE,
  CONSTRAINT `fk_curs2`
    FOREIGN KEY (`id_curs`)
    REFERENCES `platforma_de_studiu`.`cursuri` (`id_cursuri`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_profesor`
    FOREIGN KEY (`id_profesor`)
    REFERENCES `platforma_de_studiu`.`profesor` (`id_profesor`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `platforma_de_studiu`.`rol`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `platforma_de_studiu`.`rol` (
  `id_rol` INT NOT NULL,
  `tip` ENUM('student', 'profesor', 'administrator', 'super_administrator') NOT NULL,
  PRIMARY KEY (`id_rol`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `platforma_de_studiu`.`utilizator`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `platforma_de_studiu`.`utilizator` (
  `id_user` INT NOT NULL,
  `CNP` VARCHAR(13) NOT NULL,
  `nume` VARCHAR(45) NOT NULL,
  `prenume` VARCHAR(45) NOT NULL,
  `id_adrasa` INT NOT NULL,
  `telefon` VARCHAR(10) NOT NULL,
  `iban` VARCHAR(34) NOT NULL,
  `nr_contract` INT NOT NULL,
  `id_rol` INT NOT NULL,
  `parola` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id_user`),
  INDEX `fk_adresa_idx` (`id_adrasa` ASC) VISIBLE,
  INDEX `fk_rol_idx` (`id_rol` ASC) VISIBLE,
  CONSTRAINT `fk_adresa`
    FOREIGN KEY (`id_adrasa`)
    REFERENCES `platforma_de_studiu`.`adresa` (`id_adresa`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_rol`
    FOREIGN KEY (`id_rol`)
    REFERENCES `platforma_de_studiu`.`rol` (`id_rol`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;

USE `platforma_de_studiu` ;

-- -----------------------------------------------------
-- procedure InsertActivitateCalendar
-- -----------------------------------------------------

DELIMITER $$
USE `platforma_de_studiu`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `InsertActivitateCalendar`(
    IN zi ENUM('luni', 'marti', 'miercuri', 'joi', 'vineri'),
    IN data_inceput TIME,
    IN data DATE, 
    IN id_curs INT,
    IN tip ENUM('curs', 'seminar', 'laborator', 'examen', 'colocviu'),
    IN nr_max_part INT
)
BEGIN
   
    INSERT INTO calendar (zi, data_inceput, data)
    VALUES (zi, data_inceput, data); 
    
    SET @id_desfasurare = LAST_INSERT_ID();
    
    INSERT INTO activitate (id_curs, tip, id_desfasurare, nr_max_part)
    VALUES (id_curs, tip, @id_desfasurare, nr_max_part);
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure calculare_note
-- -----------------------------------------------------

DELIMITER $$
USE `platforma_de_studiu`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `calculare_note`(
    IN idc INT, 
    IN pondere_curs INT, 
    IN pondere_seminar INT, 
    IN pondere_lab INT
)
BEGIN
    UPDATE inrolare
    SET nota_finala = (
        (IFNULL(nota_curs, 0) * pondere_curs +
        IFNULL(nota_seminar, 0) * pondere_seminar +
        IFNULL(nota_lab, 0) * pondere_lab) / 100
    )
    WHERE id_curs = idc;
END$$

DELIMITER ;
USE `platforma_de_studiu`;

DELIMITER $$
USE `platforma_de_studiu`$$
CREATE
DEFINER=`root`@`localhost`
TRIGGER `platforma_de_studiu`.`before_insert_calendar`
BEFORE INSERT ON `platforma_de_studiu`.`calendar`
FOR EACH ROW
BEGIN
    SET NEW.data_sfarsit = ADDTIME(NEW.data_inceput, '02:00:00');
END$$


DELIMITER ;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
