<?php

session_start();

if(isset($_GET['error'])) {
  switch($_GET['error']) {
  case 1:
    echo "<b>Wrong password.</b>";
    break;
  default:
    echo "<b>An unknown error occured.</b>";
    break;
  }
}

if(!isset($_SESSION['loggedin']) || !$_SESSION['loggedin']) {
  $_SESSION['loggedin'] = false;
  if(isset($_POST['login'])) {
    if(md5($_POST['password']) === "f969306d174389f30cb508e21412d741") {
      $_SESSION['loggedin'] = true;
      header('Location: /moderation.php');
    } else {
      header('Location: /moderation.php?error=1');
    }
  }
  if(!$_SESSION['loggedin']) {
    echo <<<EOF
<form method="POST">
  <input type="text" name="username" />
  <input type="password" name="password" />
  <input type="submit" name="login" />
</form>
EOF;
    die();
  }
}

$host = "localhost";
$user = "neva";
$pass = "";
$name = "neva";

mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);
$db = new mysqli($host, $user, $pass, $name);
if($db->connect_errno) {
  die("DB Connection: ".$db->connect_error);
}
unset($host, $user, $pass, $name);

if(isset($_POST['accept']) || isset($_POST['reject'])) {
  if(isset($_POST['accept'])) {
    $name = $_POST['suggestion'];
    $category = (int)$_POST['category_id'];

    $sql = "SELECT MAX(`last_updated`) FROM `suggestee`";
    $res = $db->query($sql);
    $last_updated = $res->fetch_array()[0] + 1;

    $sql = "INSERT INTO `suggestee` (`category_id`, `name`, `last_updated`)
      VALUES (?, ?, ?)";
    $stmt = $db->prepare($sql);
    $stmt->bind_param("isi", $category, $name, $last_updated);
    $stmt->execute();
  }
  $prop_id = (int)$_POST['id'];
  $sql = "DELETE FROM `item_suggestion` WHERE `id`=?";
  $stmt = $db->prepare($sql);
  $stmt->bind_param("i", $prop_id);
  $stmt->execute();
  header('Location: /moderation.php');
}

if(isset($_POST['accept_tag']) || isset($_POST['reject_tag'])) {
  if(isset($_POST['accept_tag'])) {
    $name = $_POST['suggestion'];
    $sql = "INSERT INTO `tag` (`key`) VALUES (?)";
    $stmt = $db->prepare($sql);
    $stmt->bind_param("s", $name);
    $stmt->execute();
  }
  $prop_id = (int)$_POST['id'];
  $sql = "DELETE FROM `tag_suggestion` WHERE `id`=?";
  $stmt = $db->prepare($sql);
  $stmt->bind_param("i", $prop_id);
  $stmt->execute();
  header('Location: /moderation.php');
}

if(isset($_POST['accept_tvs']) || isset($_POST['reject_tvs'])) {
  if(isset($_POST['accept_tvs'])) {
    $suggestee_id = $_POST['suggestee_id'];
    $tag_id = $_POST['tag_id'];
    $sql = "INSERT INTO `suggestee_tags` (`suggestee_id`, `tag_id`) 
      VALUES (?, ?)";
    $stmt = $db->prepare($sql);
    $stmt->bind_param("ii", $suggestee_id, $tag_id);
    $stmt->execute();

    $sql = "SELECT MAX(`last_updated`) FROM `suggestee`";
    $res = $db->query($sql);
    $last_updated = $res->fetch_array()[0] + 1;

    $sql = "UPDATE `suggestee` SET `last_updated`=? WHERE `id`=?";
    $stmt = $db->prepare($sql);
    $stmt->bind_param("ii", $last_updated, $suggestee_id);
    $stmt->execute();
  }
  $prop_id = (int)$_POST['id'];
  $sql = "DELETE FROM `tag_value_suggestion` WHERE `id`=?";
  $stmt = $db->prepare($sql);
  $stmt->bind_param("i", $prop_id);
  $stmt->execute();
  header('Location: /moderation.php');
}

if(isset($_POST['add_tvs'])) {
  $suggestee = $_POST['suggestee_name'];
  $tag = $_POST['tag_name'];

  $sql = "INSERT INTO `suggestee_tags` (`suggestee_id`, `tag_id`)
    SELECT `suggestee`.`id`, `tag`.`id` FROM `suggestee`, `tag` WHERE `name`=? 
    AND `key`=?";
  $stmt = $db->prepare($sql);
  $stmt->bind_param("ss", $suggestee, $tag);
  $stmt->execute();

  $sql = "SELECT MAX(`last_updated`) FROM `suggestee`";
  $res = $db->query($sql);
  $last_updated = $res->fetch_array()[0] + 1;

  $sql = "UPDATE `suggestee` SET `last_updated`=? WHERE `name`=?";
  $stmt = $db->prepare($sql);
  $stmt->bind_param("is", $last_updated, $suggestee);
  $stmt->execute();

  header('Location: /moderation.php');
}

$sql = "SELECT `category_id`, `name` FROM `suggestee`";
$res = $db->query($sql);
$suggestees = array();
echo "<datalist id='suggestees'>";
while($suggestee = $res->fetch_array()) {
  $category_id = $suggestee[0];
  $name = $suggestee[1];
  if(!array_key_exists($category_id, $suggestees)) {
    $suggestees[$category_id] = new \Ds\Set();
  }
  $suggestees[$category_id]->add($name);
  $name = htmlspecialchars($name, ENT_QUOTES);
  echo "<option value='$name'>";
}
echo "</datalist>";

$sql = "SELECT items.`id`, `name`, `suggestion`, cats.`id` FROM
  `item_suggestion` items, `suggestion_category` cats WHERE
  items.`category_id` = cats.`id`";
$res = $db->query($sql);
if($res->num_rows==0) {
  echo "Nothing to moderate, well done.";
}

echo "<table style='float: left;'>";
while($prop = $res->fetch_array()) {
  $exists = $suggestees[$prop[3]]->contains($prop[2]);
  $color = $exists ? 'red' : 'green';
  $prop[2] = htmlspecialchars($prop[2], ENT_QUOTES);
  echo <<<EOF
<tr style='background: $color;'>
<form method="POST">
  <input type="hidden" name="id" value="$prop[0]">
  <td><label>$prop[1]</label></td>
  <td><input type="text" name="suggestion" value="$prop[2]"></td>
  <td><input type="submit" name="accept" value="accept"></td>
  <td><input type="submit" name="reject" value="reject"></td>
  <input type="hidden" name="category_id" value="$prop[3]">
</form>
</tr>
EOF;
}
echo <<<EOF
<tr>
<form method="POST">
  <input type="hidden" name="id" value="0">
  <td><label>meal</label></td>
  <td><input type="text" name="suggestion" value=""></td>
  <td><input type="submit" name="accept" value="add meal"></td>
  <input type="hidden" name="category_id" value="1">
</form>
</tr>
EOF;
echo "</table>";
unset($suggestees);

$sql = "SELECT `key` FROM `tag`";
$res = $db->query($sql);
$tags = new \Ds\Set();
echo "<datalist id='tags'>";
while($tag = $res->fetch_array()) {
  $tags->add($tag[0]);
  $tag[0] = htmlspecialchars($tag[0], ENT_QUOTES);
  echo "<option value='$tag[0]'>";
}
echo "</datalist>";

$sql = "SELECT `id`, `tag` FROM `tag_suggestion` tags";
$res = $db->query($sql);
if($res->num_rows==0) {
  echo "No tag to moderate, well done.";
}

echo "<table style='float: left;'>";
while($prop = $res->fetch_array()) {
  $exists = $tags->contains($prop[1]);
  $color = $exists ? 'red' : 'green';
  $prop[1] = htmlspecialchars($prop[1], ENT_QUOTES);
  echo <<<EOF
<tr style='background: $color;'>
<form method="POST">
  <input type="hidden" name="id" value="$prop[0]">
  <td><label>tag</label></td>
  <td><input type="text" name="suggestion" value="$prop[1]"></td>
  <td><input type="submit" name="accept_tag" value="accept"></td>
  <td><input type="submit" name="reject_tag" value="reject"></td>
</form>
</tr>
EOF;
}
echo <<<EOF
<tr>
<form method="POST">
  <input type="hidden" name="id" value="0">
  <td><label>tag</label></td>
  <td><input type="text" name="suggestion" value=""></td>
  <td><input type="submit" name="accept_tag" value="add tag"></td>
</form>
</tr>
EOF;
echo "</table>";

$sql = "SELECT `tag_value_suggestion`.`id`, `suggestee_id`, `name`, `tag_id`,
  `key` FROM  `tag_value_suggestion`, `suggestee`, `tag` WHERE 
  `suggestee_id`=`suggestee`.`id` AND `tag_id`=`tag`.`id`";
$res = $db->query($sql);
if($res->num_rows==0) {
  echo "No suggestee-tag relation to moderate, well done.";
}

echo "<table style='float: left;'>";
while($prop = $res->fetch_array()) {
  echo <<<EOF
<tr style='background: $color;'>
<form method="POST">
  <input type="hidden" name="id" value="$prop[0]">
  <input type="hidden" name="suggestee_id" value="$prop[1]">
  <input type="hidden" name="tag_id" value="$prop[3]">
  <td><label>tvs</label></td>
  <td><label>$prop[2]</label></td>
  <td><label>$prop[4]</label></td>
  <td><input type="submit" name="accept_tvs" value="accept"></td>
  <td><input type="submit" name="reject_tvs" value="reject"></td>
</form>
</tr>
EOF;
}
echo <<<EOF
<tr>
<form method="POST">
  <input type="hidden" name="id" value="0">
  <td><label>tvs</label></td>
  <td><input type="text" name="suggestee_name" list="suggestees"></td>
  <td><input type="text" name="tag_name" list="tags"></td>
  <td><input type="submit" name="add_tvs" value="accept"></td>
</form>
</tr>
EOF;
echo "</table>";
