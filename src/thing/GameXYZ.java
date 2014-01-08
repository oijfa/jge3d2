package thing;

import thing.components.PhysicsComp;
import thing.components.ModelComp;
import thing.components.PositionComp;
import thing.components.SpriteComp;
import thing.systems.PhysicsSys;
import thing.systems.ModelRenderSys;
import thing.systems.SpriteRenderSys;

import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.physics.bullet.collision.btBox2dShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btMaterial;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;

public class GameXYZ implements Screen {

	OrthographicCamera spriteCamera;
	PerspectiveCamera modelCamera;
	private World world;

	private SpriteRenderSys spriteRenderSys;
	private ModelRenderSys modelRenderSys;
	private PhysicsSys bulletPhysicsSys;

	private float delta = 0;
	private float previousTimeStamp = 0;

	@SuppressWarnings("unused")
	private int frames = 0;

	public GameXYZ(Game game) {
		world = new World();

		cameraSetup();
		systemsSetup();

		world.initialize();

		entitySetup();
	}

	private void cameraSetup() {
		modelCamera = new PerspectiveCamera(45, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		modelCamera.position.set(10f, 10f, 10f);
		modelCamera.lookAt(0, 0, 0);
		modelCamera.near = 0.1f;
		modelCamera.far = 300f;

		spriteCamera = new OrthographicCamera();
		spriteCamera.setToOrtho(false, 1280, 900);
	}

	private void systemsSetup() {
		spriteRenderSys = world.setSystem(new SpriteRenderSys(spriteCamera), true);
		modelRenderSys = world.setSystem(new ModelRenderSys(modelCamera), true);
		bulletPhysicsSys = world.setSystem(new PhysicsSys(world));
	}

	private void entitySetup() {
		// Start adding Models...
		ModelBuilder mb = new ModelBuilder();

		// Falling box
		Entity e = world.createEntity();

		Model m = mb.createBox(5f, 5f, 5f, new Material(ColorAttribute.createDiffuse(Color.RED)), Usage.Position | Usage.Normal);
		e.addComponent(new ModelComp(m));
		e.addComponent(this.makeBox());

		e.addToWorld();

		// Land
		e = world.createEntity();

		m = mb.createRect(-10f, 0f, -10f,// x00, y00, z00,
				-10f, 0f, 10f,// x10, y10, z10,
				10f, 0f, 10f,// x11, y11, z11,
				10f, 0f, -10f,// x01, y01, z01,
				0f, 1f, 0f,// normalX, normalY, normalZ,
				new Material(ColorAttribute.createDiffuse(Color.GREEN)),// material,
				Usage.Position | Usage.Normal // attributes
		);
		e.addComponent(new ModelComp(m));
		e.addComponent(this.makeLand());

		e.addToWorld();
	}

	@Override
	public void render(float delta) {
		world.setDelta(getDelta());
		bulletPhysicsSys.process();

		modelCamera.update();
		// if(use3D){
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		modelCamera.update();
		// }else{
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		spriteCamera.update();
		// }
		world.process();

		// if(use3D){
		modelRenderSys.process();
		// }else{
		spriteRenderSys.process();
		// }

		// posSys.process();
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		bulletPhysicsSys.dispose();

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resize(int arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {
		// TODO Auto-generated method stub

	}

	public float getDelta() {
		if (previousTimeStamp != 0) {
			delta = (System.nanoTime() - previousTimeStamp);
		}

		previousTimeStamp = System.nanoTime();
		frames++;

		return delta;
	}

	public PhysicsComp makeBox() {
		float mass = 1f;
		Vector3 inertia = new Vector3(0, 0, 0);

		btMotionState motionState = new btDefaultMotionState();
		btCollisionShape collisionShape = new btSphereShape(1f);

		btRigidBody rigidBody = new btRigidBody(mass, motionState, collisionShape);
		rigidBody.setWorldTransform(new Matrix4(new Vector3(0, 10, 0), new Quaternion(0, 0, 0, 1), new Vector3(1, 1, 1)));

		collisionShape.calculateLocalInertia(mass, inertia);
		rigidBody.setMassProps(mass, inertia);

		PhysicsComp b = new PhysicsComp(rigidBody);

		bulletPhysicsSys.dynamicsWorld.addRigidBody(rigidBody);

		rigidBody.updateInertiaTensor();

		return b;
	}

	private Component makeLand() {
		float mass = 0f;
		Vector3 inertia = new Vector3(0, 0, 0);

		btMotionState motionState = new btDefaultMotionState();
		btCollisionShape collisionShape = new btBox2dShape(new Vector3(10, 1, 10));

		btRigidBody rigidBody = new btRigidBody(mass, motionState, collisionShape);
		rigidBody.setWorldTransform(new Matrix4(new Vector3(10, -10, 0), new Quaternion(0, 0, 0, 1), new Vector3(1, 1, 1)));

		collisionShape.calculateLocalInertia(mass, inertia);
		rigidBody.setMassProps(mass, inertia);

		PhysicsComp b = new PhysicsComp(rigidBody);

		bulletPhysicsSys.dynamicsWorld.addRigidBody(rigidBody);

		rigidBody.updateInertiaTensor();

		return b;
	}
}
